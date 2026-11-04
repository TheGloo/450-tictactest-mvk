variable "region" {
  description = "AWS region (the Learner Lab only allows us-east-1 and us-west-2)"
  type        = string
  default     = "us-east-1"
}

variable "instance_type" {
  description = "EC2 instance type; one JVM per player needs about 40 MB"
  type        = string
  default     = "t3.micro"
}

variable "ssh_public_key_file" {
  description = "public key for the user ubuntu, its private key is the GitHub secret SSH_PRIVATE_KEY"
  type        = string
  default     = "deploy_key.pub"
}

variable "ssh_cidr" {
  description = "who may connect with SSH; GitHub-hosted runners have no fixed addresses"
  type        = string
  default     = "0.0.0.0/0"
}
