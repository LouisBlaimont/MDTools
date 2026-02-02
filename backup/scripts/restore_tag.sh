#!/usr/bin/env bash
set -euo pipefail

log() { echo "[$(date -Iseconds)] $*"; }

source /scripts/common.sh

FRONTEND_CONTAINER="${FRONTEND_CONTAINER:-svelte}"
STOP_FRONTEND="${STOP_FRONTEND:-false}"

TAG="${1:-}"
if [[ "$TAG" != "daily" && "$TAG" != "weekly" ]]; then
  echo "Usage: /scripts/restore_tag.sh daily|weekly" >&2
  exit 1
fi

log "[INFO] restore_tag.sh starting"
log "[INFO] TAG=$TAG FRONTEND_CONTAINER=$FRONTEND_CONTAINER STOP_FRONTEND=$STOP_FRONTEND"
log "[INFO] RESTIC_REPOSITORY=$RESTIC_REPOSITORY"

require_env RESTIC_REPOSITORY
require_env RESTIC_PASSWORD

require_env POSTGRES_USER
require_env POSTGRES_PASSWORD
require_env POSTGRES_DB

require_env KEYCLOAK_DB_USERNAME
require_env KEYCLOAK_DB_PASSWORD
require_env KEYCLOAK_DB_NAME

stop_services_for_restore() {
  log "[INFO] Stopping app services..."
  if [[ "$STOP_FRONTEND" == "true" ]]; then
    log "[INFO] Stopping frontend container: $FRONTEND_CONTAINER"
    docker stop "$FRONTEND_CONTAINER" >/dev/null 2>&1 || true
  else
    log "[INFO] STOP_FRONTEND=false, keeping frontend running"
  fi

  docker stop caddy      >/dev/null 2>&1 || true
  docker stop springboot >/dev/null 2>&1 || true
  docker stop keycloak   >/dev/null 2>&1 || true
}

start_services_after_restore() {
  log "[INFO] Starting app services..."
  docker start keycloak >/dev/null 2>&1 ||  { log "[ERROR] failed to start keycloak"; exit 1; }
  docker start springboot >/dev/null 2>&1 ||  { log "[ERROR] failed to start springboot"; exit 1; }

  # Frontend may be intentionally kept alive during restore
  if docker ps -a --format '{{.Names}}' | grep -qx "$FRONTEND_CONTAINER"; then
    docker start "$FRONTEND_CONTAINER" >/dev/null 2>&1 || true
  else
    log "[WARN] frontend container '$FRONTEND_CONTAINER' not found (skipping start)"
  fi

  docker start caddy >/dev/null 2>&1 ||  { log "[ERROR] failed to start caddy"; exit 1; }
}

restore_files_into_volume() {
  local src="$1"
  local dst="$2"

  log "[INFO] Restoring files from $src -> $dst"
  mkdir -p "$dst"

  # Stats are crucial to see if something is really copied or if it's empty
  rsync -a --delete --stats "$src"/ "$dst"/
}

restore_if_present() {
  local src="$1"
  local dst="$2"

  if [[ -d "$src" ]]; then
    restore_files_into_volume "$src" "$dst"
  else
    log "[WARN] Snapshot does not contain: $src (skipping)"
  fi
}

restore_dbs_from_dumps() {
  local dumps_dir="$1"

  log "[INFO] dumps dir content:"
  ls -lah "$dumps_dir" || true

  if [[ ! -f "$dumps_dir/mdtools.dump" ]]; then
    log "[ERROR] mdtools.dump not found in: $dumps_dir"
    exit 1
  fi
  if [[ ! -f "$dumps_dir/keycloak.dump" ]]; then
    log "[ERROR] keycloak.dump not found in: $dumps_dir"
    exit 1
  fi

  log "[INFO] Restoring MDTools DB from dump..."
  PGPASSWORD="$POSTGRES_PASSWORD" pg_restore \
    -h postgres -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
    --clean --if-exists "$dumps_dir/mdtools.dump"

  log "[INFO] Restoring Keycloak DB from dump..."
  PGPASSWORD="$KEYCLOAK_DB_PASSWORD" pg_restore \
    -h keycloak-db -U "$KEYCLOAK_DB_USERNAME" -d "$KEYCLOAK_DB_NAME" \
    --clean --if-exists "$dumps_dir/keycloak.dump"
}

find_dumps_dir() {
  local root="$1"

  if [[ -d "$root/dumps" ]]; then
    echo "$root/dumps"
    return 0
  fi

  local found=""
  found="$(find "$root" -maxdepth 4 -type d -name dumps 2>/dev/null | head -n 1 || true)"
  if [[ -n "$found" ]]; then
    echo "$found"
    return 0
  fi

  return 1
}

init_restic_if_needed

RESTORE_DIR="$(mktemp -d)"
log "[INFO] RESTORE_DIR=$RESTORE_DIR"

cleanup() {
  log "[INFO] Cleanup..."
  rm -rf "$RESTORE_DIR" 2>/dev/null || true
}

ensure_up() {
  log "[INFO] Ensuring services are up..."
  start_services_after_restore || true
}

stop_services_for_restore
trap 'ensure_up; cleanup' EXIT

# --- RESTIC VISIBILITY ---
log "[INFO] Available snapshots for tag=$TAG:"
restic snapshots --tag "$TAG" || { log "[ERROR] restic snapshots failed"; exit 1; }

SNAP_ID="$(restic snapshots --tag "$TAG" --last 1 --json 2>/dev/null | jq -r '.[0].short_id // empty')"
log "[INFO] Latest snapshot short_id for tag=$TAG: ${SNAP_ID:-none}"

log "[INFO] Restic restore latest snapshot tag=$TAG into $RESTORE_DIR"
restic restore latest --tag "$TAG" --target "$RESTORE_DIR" -v

log "[INFO] Top-level restored directories (maxdepth=3):"
find "$RESTORE_DIR" -maxdepth 3 -type d -print || true

# --- FILE RESTORE ---
restore_if_present "$RESTORE_DIR/data/pictures"     "/data/pictures"
restore_if_present "$RESTORE_DIR/data/caddy_data"   "/data/caddy_data"
restore_if_present "$RESTORE_DIR/data/caddy_config" "/data/caddy_config"

log "[INFO] After restore, pictures content (first entries):"
ls -lah /data/pictures | head -n 50 || true

# --- DUMPS DISCOVERY ---
log "[INFO] Searching dumps dir under $RESTORE_DIR"
DUMPS_DIR="$(find_dumps_dir "$RESTORE_DIR" || true)"
log "[INFO] DUMPS_DIR=${DUMPS_DIR:-<none>}"

if [[ -z "${DUMPS_DIR:-}" ]]; then
  log "[ERROR] dumps directory not found in restored snapshot"
  log "[INFO] Available directories under restore target (maxdepth=3):"
  find "$RESTORE_DIR" -maxdepth 3 -type d -print || true
  exit 1
fi

restore_dbs_from_dumps "$DUMPS_DIR"

start_services_after_restore

log "[INFO] Restore done tag=$TAG"
