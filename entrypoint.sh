#!/bin/sh
# entrypoint.sh - parse DATABASE_URL and wait for PostgreSQL to be ready

set -e

# Parse DATABASE_URL if available
if [ -n "$DATABASE_URL" ]; then
  # Extract host and port from DATABASE_URL (format: postgres://user:pass@host:port/db)
  DB_HOST=$(echo "$DATABASE_URL" | sed -n 's|.*@\([^:]*\):.*|\1|p')
  DB_PORT=$(echo "$DATABASE_URL" | sed -n 's|.*@[^:]*:\([0-9]*\)/.*|\1|p')
fi

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"

echo "Waiting for PostgreSQL at $DB_HOST:$DB_PORT..."

while ! nc -z "$DB_HOST" "$DB_PORT" 2>/dev/null; do
  sleep 1
done

echo "PostgreSQL is ready. Starting application..."
exec java -Dserver.port="${PORT:-8000}" -jar app.jar
