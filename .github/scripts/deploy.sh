#!/usr/bin/env bash
# Deploys TicTacTest to the EC2 instance, but only the parts that differ from what the
# server runs. The server keeps the hash of each deployed part in /opt/tictactest/.deployed-<part>.
#
#   jar         the release jar         -> replaced in /opt/tictactest/app, next game uses it
#   webconsole  webconsole/Dockerfile,  -> uploaded, image rebuilt, container restarted
#               webconsole/compose.yaml
#
# Usage: .github/scripts/deploy.sh <host> <jar>       (run from the repository root)
# SSH_KEY: optional private key file, default is the ssh configuration of the caller.
set -euo pipefail

host=$1
jar=$2
dir=/opt/tictactest

ssh_opts=(-o StrictHostKeyChecking=accept-new -o BatchMode=yes)
if [[ -n "${SSH_KEY:-}" ]]; then
  ssh_opts+=(-i "$SSH_KEY")
fi

remote() { ssh "${ssh_opts[@]}" "ubuntu@$host" "$@"; }
upload() { scp -q "${ssh_opts[@]}" "$@"; }
deployed() { remote "cat $dir/.deployed-$1 2>/dev/null || true"; }

summary() {
  echo "$1: $2"
  if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
    echo "| $1 | $2 |" >> "$GITHUB_STEP_SUMMARY"
  fi
}

# a new instance installs Docker first (infra/cloud-init.yaml)
remote cloud-init status --wait > /dev/null

# The hash of the jar covers the names and contents of its entries, not the zip file:
# a rebuild of the same code differs only in the timestamps inside the zip.
jar_hash=$({ unzip -Z1 "$jar"; unzip -p "$jar"; } | sha256sum | cut -d' ' -f1)
webconsole_hash=$(cat webconsole/Dockerfile webconsole/compose.yaml | sha256sum | cut -d' ' -f1)

# jar first: a newly built container finds the game right away
if [[ "$(deployed jar)" == "$jar_hash" ]]; then
  summary jar "unverändert, übersprungen"
else
  upload "$jar" "ubuntu@$host:$dir/app/.tictactoe.jar.new"
  # mv is atomic: running games keep the old file, new connections get the new one
  remote "mv -f $dir/app/.tictactoe.jar.new $dir/app/tictactoe.jar && echo $jar_hash > $dir/.deployed-jar"
  summary jar "deployt ($(basename "$jar"))"
fi

if [[ "$(deployed webconsole)" == "$webconsole_hash" ]]; then
  summary webconsole "unverändert, übersprungen"
else
  upload webconsole/Dockerfile webconsole/compose.yaml "ubuntu@$host:$dir/"
  remote "cd $dir && WEB_PORT=80 docker compose up --detach --build --remove-orphans --quiet-pull \
    && echo $webconsole_hash > $dir/.deployed-webconsole"
  summary webconsole "deployt (Image neu gebaut)"
fi

echo "TicTacTest: http://$host/"
