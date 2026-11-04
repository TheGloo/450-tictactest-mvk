output "instance_id" {
  description = "ID der EC2-Instanz"
  value       = aws_instance.tictactest.id
}

output "public_ip" {
  description = "Öffentliche IP der EC2-Instanz"
  value       = aws_instance.tictactest.public_ip
}
