#!/bin/sh
set -eu
# The database CA is a public certificate supplied by the hosting environment.
if [ -n "${POSTGRES_CA_CERT:-}" ]; then
  umask 077
  printf '%s\n' "$POSTGRES_CA_CERT" > /tmp/streamhub-postgres-ca.pem
fi
exec "$@"
