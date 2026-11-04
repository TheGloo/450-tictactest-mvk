variable "region" {
  description = "AWS-Region"
  type        = string
  default     = "eu-central-2"
}

variable "instance_type" {
  description = "EC2-Instanztyp"
  type        = string
  default     = "t3.micro"
}

variable "key_name" {
  description = "Name eines bestehenden EC2-Key-Pairs für SSH (null = kein SSH-Key)"
  type        = string
  default     = null
}

variable "ssh_cidr" {
  description = "CIDR, aus dem SSH (Port 22) erlaubt ist, z. B. die eigene IP mit /32"
  type        = string
}
