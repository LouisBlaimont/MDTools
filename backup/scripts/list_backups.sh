#!/usr/bin/env bash
set -euo pipefail

require_env() {
  local name="$1"
  if [[ -z "${!name:-}" ]]; then
    echo "Missing env var: $name" >&2
    exit 1
  fi
}

require_env RESTIC_REPOSITORY
require_env RESTIC_PASSWORD

latest_for_tag() {
  local tag="$1"
  restic snapshots --json --tag "$tag" 2>/dev/null \
    | jq -r 'sort_by(.time) | last // empty'
}

daily="$(latest_for_tag daily || true)"
weekly="$(latest_for_tag weekly || true)"

jq -n \
  --argjson daily "${daily:-null}" \
  --argjson weekly "${weekly:-null}" \
  '{daily:$daily, weekly:$weekly}'
