#!/usr/bin/env bash
set -euo pipefail

require_env() {
  local name="$1"
  if [[ -z "${!name:-}" ]]; then
    echo "Missing required env var: $name" >&2
    exit 1
  fi
}

init_restic_if_needed() {
  if ! restic snapshots >/dev/null 2>&1; then
    echo "[INFO] Initializing restic repository at $RESTIC_REPOSITORY"
    restic init
  fi
}

make_db_dumps() {
  local dump_dir="$1"
  mkdir -p "$dump_dir"

  require_env POSTGRES_USER
  require_env POSTGRES_PASSWORD
  require_env POSTGRES_DB

  require_env KEYCLOAK_DB_USERNAME
  require_env KEYCLOAK_DB_PASSWORD
  require_env KEYCLOAK_DB_NAME

  echo "[INFO] Dumping MDTools PostgreSQL..."
  PGPASSWORD="$POSTGRES_PASSWORD" pg_dump \
    -h postgres -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
    -Fc -f "$dump_dir/mdtools.dump"

  echo "[INFO] Dumping Keycloak PostgreSQL..."
  PGPASSWORD="$KEYCLOAK_DB_PASSWORD" pg_dump \
    -h keycloak-db -U "$KEYCLOAK_DB_USERNAME" -d "$KEYCLOAK_DB_NAME" \
    -Fc -f "$dump_dir/keycloak.dump"
}

keep_only_last_snapshot_for_tag() {
  local tag="$1"
  echo "[INFO] Keeping only the latest snapshot for tag=$tag"
  restic forget --tag "$tag" --keep-last 1 --prune
}
