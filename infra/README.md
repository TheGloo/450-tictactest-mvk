# Infrastruktur (OpenTofu)

Minimales Grundgerüst: **eine leere EC2-Instanz** (Ubuntu 24.04, `t3.micro`) im
Default-VPC mit einer Security Group, die nur SSH aus `ssh_cidr` erlaubt. Docker,
die Web-Konsole ([`../webconsole/`](../webconsole/README.md)), weitere Ports, TLS und
Auth gehören nicht hierher, sondern in die Folgeaufträge.

| Datei | Inhalt |
|---|---|
| `versions.tf` | OpenTofu ≥ 1.8, Provider `hashicorp/aws` ~> 5.0 |
| `variables.tf` | `region` (Default `eu-central-2`), `instance_type`, `key_name`, `ssh_cidr` |
| `main.tf` | AMI-Lookup (Canonical), Security Group, `aws_instance` |
| `outputs.tf` | `instance_id`, `public_ip` |
| `terraform.tfvars.example` | Vorlage für `terraform.tfvars` |

## Verwendung

Voraussetzungen: [OpenTofu](https://opentofu.org) (`tofu`), AWS-Zugangsdaten in der
Umgebung (z. B. `AWS_PROFILE`), ein bestehendes EC2-Key-Pair.

```bash
cd infra
cp terraform.tfvars.example terraform.tfvars   # ssh_cidr und key_name eintragen
tofu init
tofu plan
tofu apply                                     # gibt public_ip aus
ssh ubuntu@$(tofu output -raw public_ip)
tofu destroy                                   # alles wieder entfernen
```

Der Zustand (`terraform.tfstate`) liegt lokal und wird nicht committet
(`.gitignore`). Zugangsdaten gehören nie ins Repository.
