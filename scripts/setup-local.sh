#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [ -e .env ]; then
  echo '.env already exists; preserving it.'
  exit 0
fi
umask 077
{
  echo "DATABASE_PASSWORD=$(openssl rand -hex 24)"
  echo "JWT_SECRET=$(openssl rand -hex 32)"
  echo "CATALOG_API_KEY=$(openssl rand -hex 32)"
  echo "ADMIN_API_KEY=$(openssl rand -hex 32)"
  echo 'CORS_ORIGIN=http://localhost:8088'
} > .env
echo 'Created .env with fresh local secrets. Run: docker compose up --build -d'
