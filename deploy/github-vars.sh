#!/usr/bin/env bash
# After `tofu apply`: copies the outputs the pipeline needs into GitHub Variables.
# They are not secret (see infra/outputs.tf). Needs the GitHub CLI (gh auth login).
set -euo pipefail

cd "$(dirname "$0")/../infra"
for name in instance_id artifact_bucket frontend_bucket web_console_url frontend_url; do
  value=$(tofu output -raw "$name")
  gh variable set "${name^^}" --body "$value"
  echo "${name^^}=$value"
done
