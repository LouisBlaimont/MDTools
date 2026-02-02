#!/usr/bin/env bash
set -euo pipefail
source /scripts/common.sh

TAG="${1:-}"
if [[ "$TAG" != "daily" && "$TAG" != "weekly" ]]; then
  echo "Usage: /scripts/backup_tag.sh daily|weekly" >&2
  exit 1
fi

require_env RESTIC_REPOSITORY
require_env RESTIC_PASSWORD

init_restic_if_needed

TMP_DIR="$(mktemp -d)"
trap "rm -rf \"$TMP_DIR\"" EXIT

make_db_dumps "$TMP_DIR/dumps"

echo "[INFO] Creating restic snapshot tag=$TAG"
restic backup \
  --tag "$TAG" \
  /data/pictures \
  /data/caddy_data \
  /data/caddy_config \
  "$TMP_DIR/dumps"

keep_only_last_snapshot_for_tag "$TAG"

echo "[INFO] Backup done tag=$TAG"
