#!/usr/bin/env bash
set -euo pipefail

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker Engine is not installed. Install it before starting this bundle." >&2
  exit 1
fi

if [ ! -f vacation-images.tar ]; then
  echo "vacation-images.tar is missing; run this script from the copied bundle directory." >&2
  exit 1
fi

if grep -q 'REPLACE_WITH_' .env 2>/dev/null; then
  echo "Fill in real secrets in .env before starting." >&2
  exit 1
fi

docker load --input vacation-images.tar
docker compose --env-file .env -f docker-compose.yml up -d
docker compose --env-file .env -f docker-compose.yml ps
echo "Health check: curl http://localhost:${BACKEND_EXTERNAL_PORT:-8090}/actuator/health"
