#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8082}"

check() {
  local path="$1"
  printf 'GET %s%s\n' "$BASE_URL" "$path"
  curl -fsS "$BASE_URL$path" >/dev/null
}

check_auth() {
  local path="$1"
  printf 'GET %s%s\n' "$BASE_URL" "$path"
  curl -fsS -H "Authorization: Bearer $TOKEN" "$BASE_URL$path" >/dev/null
}

check /actuator/health

EMAIL="smoke-$(date +%s)@mindarena.local"
REGISTER_RESPONSE="$(
  curl -fsS \
    -H 'Content-Type: application/json' \
    -d "{\"fullName\":\"Smoke User\",\"email\":\"$EMAIL\",\"password\":\"smoke-password\",\"skills\":\"smoke\",\"interests\":\"smoke\"}" \
    "$BASE_URL/api/identity/auth/register"
)"
TOKEN="$(printf '%s' "$REGISTER_RESPONSE" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')"
REFRESH_TOKEN="$(printf '%s' "$REGISTER_RESPONSE" | sed -n 's/.*"refreshToken":"\([^"]*\)".*/\1/p')"

if [ -z "$TOKEN" ]; then
  printf 'Register response did not include a token.\n' >&2
  exit 1
fi

if [ -z "$REFRESH_TOKEN" ]; then
  printf 'Register response did not include a refresh token.\n' >&2
  exit 1
fi

REFRESH_RESPONSE="$(
  curl -fsS \
    -H 'Content-Type: application/json' \
    -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}" \
    "$BASE_URL/api/identity/auth/refresh"
)"
TOKEN="$(printf '%s' "$REFRESH_RESPONSE" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')"

if [ -z "$TOKEN" ]; then
  printf 'Refresh response did not include a token.\n' >&2
  exit 1
fi

check_auth /api/identity/auth/me
check_auth /api/challenges/arenas
check_auth /api/leaderboards
check_auth /api/submissions
check_auth '/api/chat/messages?roomType=GLOBAL'

printf 'Microservices smoke checks passed.\n'
