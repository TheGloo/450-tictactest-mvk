# Where and how TicTacTest runs: EC2 instance with Java, ttyd and nginx, S3 for the
# release artifacts and for the frontend. Which version runs is decided by the
# deployment (deploy/), not here: a new release needs neither `tofu apply` nor a new
# instance.

locals {
  ttyd_version = "1.7.7"
  ttyd_sha256  = "8a217c968aba172e0dbf3f34447218dc015bc4d5e59bf51db2f2cd12b7be4f55"
}

data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }
}

data "aws_vpc" "default" {
  default = true
}

# The Learner Lab does not allow own IAM roles: the instance uses the existing
# LabInstanceProfile (LabRole) for S3 and SSM, there are no access keys on it.
data "aws_iam_instance_profile" "lab" {
  name = "LabInstanceProfile"
}

# --- buckets
# Bucket names are global: a random suffix instead of a fixed name. The resource
# aws_s3_bucket cannot be used in the Learner Lab: after creating a bucket it reads its
# object lock configuration, which a service control policy denies (403). The buckets are
# therefore created and removed with the AWS CLI; everything else about them (public
# access, policy, website, objects) is managed by the provider as usual.

resource "random_id" "bucket_suffix" {
  byte_length = 6
}

resource "terraform_data" "artifact_bucket" {
  input = "tictactest-artifacts-${random_id.bucket_suffix.hex}"

  provisioner "local-exec" {
    command = "aws s3api create-bucket --bucket ${self.input} --region ${var.region}"
  }

  provisioner "local-exec" {
    when    = destroy
    command = "aws s3 rb s3://${self.input} --force"
  }
}

resource "terraform_data" "frontend_bucket" {
  input = "tictactest-frontend-${random_id.bucket_suffix.hex}"

  provisioner "local-exec" {
    command = "aws s3api create-bucket --bucket ${self.input} --region ${var.region}"
  }

  provisioner "local-exec" {
    when    = destroy
    command = "aws s3 rb s3://${self.input} --force"
  }
}

locals {
  artifact_bucket = terraform_data.artifact_bucket.output
  frontend_bucket = terraform_data.frontend_bucket.output
}

# --- artifacts: private bucket, the pipeline uploads the release jar, the instance reads it

resource "aws_s3_bucket_public_access_block" "artifacts" {
  bucket                  = local.artifact_bucket
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# --- web console

resource "aws_security_group" "web" {
  name        = "tictactest-web"
  description = "TicTacTest web console"
  vpc_id      = data.aws_vpc.default.id
}

resource "aws_vpc_security_group_ingress_rule" "http" {
  security_group_id = aws_security_group.web.id
  description       = "web console and /version (nginx); no SSH, deployment runs over SSM"
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "tcp"
  from_port         = 80
  to_port           = 80
}

resource "aws_vpc_security_group_egress_rule" "https" {
  security_group_id = aws_security_group.web.id
  description       = "S3, SSM, package repository, ttyd download from GitHub"
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "tcp"
  from_port         = 443
  to_port           = 443
}

resource "aws_instance" "web" {
  ami                    = data.aws_ami.al2023.id
  instance_type          = var.instance_type
  iam_instance_profile   = data.aws_iam_instance_profile.lab.name
  vpc_security_group_ids = [aws_security_group.web.id]

  # Java, ttyd, nginx, systemd unit and the deploy command, see the template.
  # A change builds a new instance; it installs the last deployed version from S3.
  user_data = templatefile("${path.module}/cloud-init.yaml.tftpl", {
    region          = var.region
    artifact_bucket = local.artifact_bucket
    ttyd_version    = local.ttyd_version
    ttyd_sha256     = local.ttyd_sha256
    max_players     = var.max_players
  })
  user_data_replace_on_change = true

  root_block_device {
    volume_size = 8
    volume_type = "gp3"
  }

  metadata_options {
    http_tokens = "required" # IMDSv2 only
  }

  tags = {
    Name = "tictactest-web"
  }

  lifecycle {
    # a newer Amazon Linux image alone should not replace the running server
    ignore_changes = [ami]
  }
}

# fixed address: survives the stop/start of the Learner Lab session
resource "aws_eip" "web" {
  instance = aws_instance.web.id
  domain   = "vpc"

  tags = {
    Name = "tictactest-web"
  }
}

# --- frontend: public S3 static website

resource "aws_s3_bucket_public_access_block" "frontend" {
  bucket                  = local.frontend_bucket
  block_public_acls       = true
  ignore_public_acls      = true
  block_public_policy     = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket_policy" "frontend" {
  bucket = local.frontend_bucket
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Sid       = "PublicRead"
      Effect    = "Allow"
      Principal = "*"
      Action    = "s3:GetObject"
      Resource  = "arn:aws:s3:::${local.frontend_bucket}/*"
    }]
  })

  depends_on = [aws_s3_bucket_public_access_block.frontend]
}

resource "aws_s3_bucket_website_configuration" "frontend" {
  bucket = local.frontend_bucket

  index_document {
    suffix = "index.html"
  }
}

# Only the infrastructure knows the address of the instance, so OpenTofu writes the
# configuration of the frontend. The frontend deployment excludes this file.
resource "aws_s3_object" "frontend_config" {
  bucket        = local.frontend_bucket
  key           = "config.js"
  content_type  = "application/javascript"
  cache_control = "no-store"
  content       = <<-EOT
    // written by OpenTofu (infra/main.tf), do not deploy over it
    window.TICTACTEST_CONFIG = {
      webConsoleUrl: "http://${aws_eip.web.public_dns}/",
    };
  EOT
}
