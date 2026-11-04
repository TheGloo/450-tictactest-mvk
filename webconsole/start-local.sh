#!/usr/bin/env bash
# Starts TicTacTest locally only from the assets of a GitHub release, nothing is built.
# Usage: webconsole/start-local.sh v1.1.0
set -euo pipefail

tag=$1
version=${tag#v}
repo=https://github.com/TheGloo/450-tictactest-mvk/releases/download/$tag
cd "$(dirname "$0")"

rm -rf app site
mkdir -p app site

curl -fsSL -o app/tictactoe.jar "$repo/tictactoe-$version.jar"
# the version comes from the release tag, not from a file someone maintains
echo "$version" > app/version.txt

curl -fsSL -o site.zip "$repo/tictactest-frontend-$version.zip"
unzip -q site.zip -d site && rm site.zip
# local configuration: the archive has no config.js, every environment brings its own
cp ../frontend/config.js site/config.js

docker compose up --detach --build
echo "Frontend:    http://localhost:8000"
echo "Web-Konsole: http://localhost:8080   Version: http://localhost:8080/version"
