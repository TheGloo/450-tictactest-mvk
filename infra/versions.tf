terraform {
  required_version = ">= 1.10"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }

  # The state lives in S3 so the release pipeline and a local `tofu apply` see the same
  # infrastructure. The bucket is created once by hand (docs/DEPLOYMENT.md), it cannot
  # be part of the configuration whose state it stores. use_lockfile: lock without DynamoDB.
  backend "s3" {
    bucket       = "tictactest-tfstate-730335265764"
    key          = "tictactest/terraform.tfstate"
    region       = "us-east-1"
    use_lockfile = true
  }
}

provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project = "tictactest"
    }
  }
}
