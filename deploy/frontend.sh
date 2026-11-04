#!/usr/bin/env bash
# Deploys the frontend archive of a release to the S3 bucket and checks that the new
# version is delivered. Used by hand (docs/deployment.md) and by .github/workflows/deploy.yml.
#
# Usage: deploy/frontend.sh <version> <zip>         e.g. deploy/frontend.sh 1.2.0 tictactest-frontend-1.2.0.zip
# Needs AWS credentials and FRONTEND_BUCKET, FRONTEND_URL (tofu outputs).
set -euo pipefail

version=$1
zip=$2
site=$(mktemp -d)
trap 'rm -rf "$site"' EXIT

unzip -q "$zip" -d "$site"

# --delete removes files the new release no longer has; config.js belongs to the
# infrastructure (written by OpenTofu) and is neither uploaded nor deleted
aws s3 sync --only-show-errors --delete --exclude config.js \
  --cache-control "no-cache" "$site" "s3://$FRONTEND_BUCKET/"

for _ in $(seq 1 12); do
  delivered=$(curl -fsS --max-time 5 "${FRONTEND_URL}version.json" | sed -n 's/.*"version" *: *"\([^"]*\)".*/\1/p' || true)
  if [[ "$delivered" == "$version" ]]; then
    curl -fsS --max-time 5 -o /dev/null "${FRONTEND_URL}config.js" \
      || { echo "frontend: config.js is missing" >&2; exit 1; }
    echo "frontend $version is delivered: $FRONTEND_URL"
    exit 0
  fi
  sleep 5
done
echo "frontend: expected $version, the bucket delivers '$delivered'" >&2
exit 1
