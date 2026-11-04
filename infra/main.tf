# One EC2 instance with Docker. The application (web console + jar) is not part of the
# infrastructure, .github/scripts/deploy.sh puts it on the server.

data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*"]
  }
}

data "aws_vpc" "default" {
  default = true
}

resource "aws_key_pair" "deploy" {
  key_name   = "tictactest-deploy"
  public_key = file("${path.module}/${var.ssh_public_key_file}")
}

resource "aws_security_group" "web" {
  name        = "tictactest-web"
  description = "TicTacTest web console (HTTP) and deployment (SSH)"
  vpc_id      = data.aws_vpc.default.id
}

resource "aws_vpc_security_group_ingress_rule" "http" {
  security_group_id = aws_security_group.web.id
  description       = "web console"
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "tcp"
  from_port         = 80
  to_port           = 80
}

resource "aws_vpc_security_group_ingress_rule" "ssh" {
  security_group_id = aws_security_group.web.id
  description       = "deployment"
  cidr_ipv4         = var.ssh_cidr
  ip_protocol       = "tcp"
  from_port         = 22
  to_port           = 22
}

resource "aws_vpc_security_group_egress_rule" "all" {
  security_group_id = aws_security_group.web.id
  description       = "packages, container images, ttyd download"
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

resource "aws_instance" "web" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.instance_type
  key_name               = aws_key_pair.deploy.key_name
  vpc_security_group_ids = [aws_security_group.web.id]

  # Docker and the directory /opt/tictactest, see cloud-init.yaml. A change there builds
  # a new, empty instance; the next deploy then installs everything (no hash on the server).
  user_data                   = file("${path.module}/cloud-init.yaml")
  user_data_replace_on_change = true

  root_block_device {
    volume_size = 10
    volume_type = "gp3"
  }

  metadata_options {
    http_tokens = "required" # IMDSv2 only
  }

  tags = {
    Name = "tictactest-web"
  }

  lifecycle {
    # a newer Ubuntu image alone should not replace the running server
    ignore_changes = [ami]
  }
}

# fixed address: survives the stop/start of the Learner Lab and a replaced instance
resource "aws_eip" "web" {
  instance = aws_instance.web.id
  domain   = "vpc"

  tags = {
    Name = "tictactest-web"
  }
}
