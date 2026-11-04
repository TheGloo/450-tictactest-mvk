#!/usr/bin/env bash
# Deploys the jar of a release to the EC2 instance and checks that exactly this version
# runs. Used by hand (docs/deployment.md) and by .github/workflows/deploy.yml.
#
# Usage: deploy/backend.sh <version> <jar>          e.g. deploy/backend.sh 1.2.0 tictactoe-1.2.0.jar
# Needs AWS credentials and INSTANCE_ID, ARTIFACT_BUCKET, WEB_CONSOLE_URL (tofu outputs).
set -euo pipefail
export MSYS_NO_PATHCONV=1 # Git Bash on Windows: keep /usr/local/... as it is

version=$1
jar=$2

# 1. artifact into the private bucket, the instance fetches it with its instance profile
aws s3 cp --only-show-errors "$jar" "s3://$ARTIFACT_BUCKET/releases/$version/tictactoe.jar"

# 2. install on the instance over SSM: no SSH port, no key, the AWS credentials suffice
command_id=$(aws ssm send-command \
  --instance-ids "$INSTANCE_ID" \
  --document-name AWS-RunShellScript \
  --comment "TicTacTest backend $version" \
  --parameters "commands=[\"/usr/local/bin/tictactest-deploy $version\"]" \
  --query Command.CommandId --output text)

if ! aws ssm wait command-executed --command-id "$command_id" --instance-id "$INSTANCE_ID"; then
  aws ssm get-command-invocation --command-id "$command_id" --instance-id "$INSTANCE_ID" \
    --query '[Status, StandardOutputContent, StandardErrorContent]' --output text
  echo "backend $version: deployment on the instance failed" >&2
  exit 1
fi

# 3. check from outside: the web console must report exactly this version
for _ in $(seq 1 24); do
  running=$(curl -fsS --max-time 5 "${WEB_CONSOLE_URL}version" || true)
  if [[ "$running" == "$version" ]]; then
    echo "backend $version runs: ${WEB_CONSOLE_URL}version"
    exit 0
  fi
  sleep 5
done
echo "backend: expected $version, the instance reports '$running'" >&2
exit 1
