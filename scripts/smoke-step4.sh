#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8082}"

check() {
  local path="$1"
  printf 'GET %s%s\n' "$BASE_URL" "$path"
  curl -fsS "$BASE_URL$path" >/dev/null
}

check /actuator/health
check /api/identity/users
check /api/challenges/arenas
check /api/leaderboards
check /api/notifications
check /api/submissions
check '/api/chat/messages?roomType=GLOBAL'

printf 'Microservices smoke checks passed.\n'
