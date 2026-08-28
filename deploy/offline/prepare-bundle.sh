#!/usr/bin/env bash
set -euo pipefail

# Run on a computer with Docker and internet access.
# Produces a directory that can be copied to a USB drive.
BUNDLE_DIR="${1:-offline-bundle}"
TARGET_PLATFORM="${TARGET_PLATFORM:-linux/amd64}"

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is required on the computer preparing the bundle." >&2
  exit 1
fi

if [ -e "$BUNDLE_DIR" ]; then
  echo "Target already exists: $BUNDLE_DIR. Choose another directory or remove it yourself." >&2
  exit 1
fi

docker pull --platform "$TARGET_PLATFORM" postgres:16-alpine
docker build --platform "$TARGET_PLATFORM" --tag vacation-backend:offline .

mkdir -p "$BUNDLE_DIR"
cp deploy/offline/docker-compose.yml "$BUNDLE_DIR/"
cp deploy/offline/.env.template "$BUNDLE_DIR/.env"
cp deploy/offline/start-server.sh "$BUNDLE_DIR/"
cp deploy/offline/README.md "$BUNDLE_DIR/"
docker save --output "$BUNDLE_DIR/vacation-images.tar" \
  vacation-backend:offline postgres:16-alpine

echo "Offline bundle created: $BUNDLE_DIR"
echo "Target platform: $TARGET_PLATFORM"
echo "Copy this entire directory to the server after Docker Engine is installed there."
