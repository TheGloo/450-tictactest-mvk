variable "region" {
  description = "AWS region (the Learner Lab only allows us-east-1 and us-west-2)"
  type        = string
  default     = "us-east-1"
}

variable "instance_type" {
  description = "EC2 instance type; t3.micro has 1 GB RAM, one JVM per player needs about 60 MB"
  type        = string
  default     = "t3.micro"
}

variable "max_players" {
  description = "simultaneous games, every game is its own JVM"
  type        = number
  default     = 10
}
