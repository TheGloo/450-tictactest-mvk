output "public_ip" {
  description = "fixed address of the server, target of the deployment"
  value       = aws_eip.web.public_ip
}

output "url" {
  description = "TicTacTest in the browser"
  value       = "http://${aws_eip.web.public_ip}/"
}
