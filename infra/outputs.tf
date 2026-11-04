# None of these values is confidential: URLs, bucket names and the instance ID are
# useless without AWS credentials. deploy/github-vars.sh copies them into GitHub
# Variables (not Secrets) for the pipeline.

output "frontend_url" {
  description = "TicTacTest in the browser"
  value       = "http://${aws_s3_bucket_website_configuration.frontend.website_endpoint}/"
}

output "web_console_url" {
  description = "web console (ttyd behind nginx); the DNS name, raw IPs are blocked by some web filters"
  value       = "http://${aws_eip.web.public_dns}/"
}

output "version_url" {
  description = "version of the running jar"
  value       = "http://${aws_eip.web.public_dns}/version"
}

output "instance_id" {
  description = "target of `aws ssm send-command` in the backend deployment"
  value       = aws_instance.web.id
}

output "artifact_bucket" {
  description = "private bucket for the release jars"
  value       = local.artifact_bucket
}

output "frontend_bucket" {
  description = "public bucket of the frontend"
  value       = local.frontend_bucket
}

output "deploy_env" {
  description = "for a manual deployment: eval \"$(tofu -chdir=infra output -raw deploy_env)\""
  value       = <<-EOT
    export INSTANCE_ID=${aws_instance.web.id}
    export ARTIFACT_BUCKET=${local.artifact_bucket}
    export FRONTEND_BUCKET=${local.frontend_bucket}
    export WEB_CONSOLE_URL=http://${aws_eip.web.public_dns}/
    export FRONTEND_URL=http://${aws_s3_bucket_website_configuration.frontend.website_endpoint}/
    export AWS_REGION=${var.region}
  EOT
}
