#!/bin/sh
# entrypoint.sh - parse DATABASE_URL and wait for PostgreSQL to be ready

set -e

echo "=== Bulletin Gestion - demarrage ==="

# Parse DATABASE_URL if available
if [ -n "$DATABASE_URL" ]; then
  # Extract host and port from DATABASE_URL (format: postgres://user:pass@host:port/db)
  DB_HOST=$(echo "$DATABASE_URL" | sed -n 's|.*@\([^:]*\):.*|\1|p')
  DB_PORT=$(echo "$DATABASE_URL" | sed -n 's|.*@[^:]*:\([0-9]*\)/.*|\1|p')
fi

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"

echo "DATABASE_URL defini: $([ -n "$DATABASE_URL" ] && echo oui || echo NON)"
echo "Attente de PostgreSQL a $DB_HOST:$DB_PORT (max 60s)..."

# Attente bornee : au pire l'app demarre et Flyway/Hikari retenteront
TRIES=0
MAX_TRIES=60
until nc -z "$DB_HOST" "$DB_PORT" 2>/dev/null; do
  TRIES=$((TRIES + 1))
  if [ "$TRIES" -ge "$MAX_TRIES" ]; then
    echo "ATTENTION: PostgreSQL injoignable apres ${MAX_TRIES}s a $DB_HOST:$DB_PORT."
    echo "Demarrage quand meme (Hikari/Flyway retenteront la connexion)."
    break
  fi
  sleep 1
done

[ "$TRIES" -lt "$MAX_TRIES" ] && echo "PostgreSQL est pret."

echo "PORT=${PORT:-8000} | PROFILE=${SPRING_PROFILES_ACTIVE:-prod}"
echo "JWT_SECRET defini: $([ -n "$JWT_SECRET" ] && echo oui || echo NON)"
echo "SMTP_HOST defini: $([ -n "$SMTP_HOST" ] && echo oui || echo NON)"
echo "Demarrage de l'application..."

exec java \
  -Dserver.port="${PORT:-8000}" \
  -Dspring.profiles.active="${SPRING_PROFILES_ACTIVE:-prod}" \
  -Xmx512m \
  -Xms256m \
  -XX:+UseContainerSupport \
  -jar app.jar
