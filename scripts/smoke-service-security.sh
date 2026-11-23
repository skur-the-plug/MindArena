#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
env_file="${ENV_FILE:-$root_dir/.env}"
gateway_url="${GATEWAY_URL:-http://127.0.0.1:8082}"
identity_url="${IDENTITY_URL:-http://127.0.0.1:18081}"
challenge_url="${CHALLENGE_URL:-http://127.0.0.1:18082}"
submission_url="${SUBMISSION_URL:-http://127.0.0.1:18083}"
challenge_id="${CHALLENGE_ID:-99001}"
expired_challenge_id="$((challenge_id + 1))"
response_file="$(mktemp)"
trap 'rm -f "$response_file"' EXIT

env_value() {
    local key="$1"
    sed -n "s/^${key}=//p" "$env_file" | tail -1
}

expect_status() {
    local expected="$1"
    shift
    local actual
    actual="$(curl -sS -o "$response_file" -w '%{http_code}' "$@")"
    if [[ "$actual" != "$expected" ]]; then
        printf 'Expected HTTP %s but received %s.\n' "$expected" "$actual" >&2
        sed -n '1,12p' "$response_file" >&2
        exit 1
    fi
}

db_sql() {
    local sql="$1"
    docker compose -f "$root_dir/docker-compose.yml" -f "$root_dir/docker-compose.security-test.yml" \
        exec -T db sh -c 'mariadb -N -B -uroot -p"$MARIADB_ROOT_PASSWORD" -e "$1"' sh "$sql"
}

client_id="$(env_value SUBMISSION_SERVICE_CLIENT_ID)"
client_secret="$(env_value SUBMISSION_SERVICE_CLIENT_SECRET)"
if [[ -z "$client_id" || -z "$client_secret" ]]; then
    echo "Service credentials are missing; run scripts/generate-service-credentials.sh --write-env .env" >&2
    exit 1
fi

sql="INSERT INTO mindarena_challenge.arena (id,name,description,color) VALUES (99,'Security Test Arena','Service security smoke fixture','green') ON DUPLICATE KEY UPDATE name=VALUES(name); INSERT INTO mindarena_challenge.challenge (id,title,brief,arena_id,difficulty,deadline,active) VALUES (${challenge_id},'Secure service flow','Runtime proof for the three-service flow',99,'Advanced',DATE_ADD(NOW(), INTERVAL 1 DAY),1) ON DUPLICATE KEY UPDATE deadline=VALUES(deadline),active=1; INSERT INTO mindarena_challenge.challenge (id,title,brief,arena_id,difficulty,deadline,active) VALUES (${expired_challenge_id},'Expired secure flow','Negative runtime fixture',99,'Advanced',DATE_SUB(NOW(), INTERVAL 1 DAY),1) ON DUPLICATE KEY UPDATE deadline=VALUES(deadline),active=1;"
db_sql "$sql" >/dev/null

email="security-smoke-$(date +%s)@mindarena.local"
registration="$(curl -fsS -H 'Content-Type: application/json' \
    -d "{\"fullName\":\"Security Smoke User\",\"email\":\"$email\",\"password\":\"smoke-password\"}" \
    "$gateway_url/api/identity/auth/register")"
user_token="$(printf '%s' "$registration" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')"
user_id="$(printf '%s' "$registration" | sed -n 's/.*"user":{"id":\([0-9]*\).*/\1/p')"

identity_token_response="$(curl -fsS -u "$client_id:$client_secret" -X POST \
    "$identity_url/internal/security/token?audience=mindarena-identity-service")"
identity_token="$(printf '%s' "$identity_token_response" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')"
challenge_token_response="$(curl -fsS -u "$client_id:$client_secret" -X POST \
    "$identity_url/internal/security/token?audience=mindarena-challenge-service")"
challenge_token="$(printf '%s' "$challenge_token_response" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')"

if [[ -z "$user_token" || -z "$user_id" || -z "$identity_token" || -z "$challenge_token" ]]; then
    echo "A required test token or user identifier was not returned." >&2
    exit 1
fi

expect_status 401 "$identity_url/internal/users/$user_id/verify"
expect_status 401 -H 'Authorization: Bearer malformed' "$identity_url/internal/users/$user_id/verify"
expect_status 401 -H "Authorization: Bearer $user_token" "$identity_url/internal/users/$user_id/verify"
expect_status 401 -H 'X-User-Id: 1' -H 'X-User-Role: ADMIN' "$identity_url/internal/users/$user_id/verify"
expect_status 401 -H "X-User-Id: $user_id" -H 'X-User-Role: USER' "$submission_url/api/submissions"
expect_status 401 -H "Authorization: Bearer $challenge_token" "$submission_url/api/submissions"
expect_status 403 -H "Authorization: Bearer $identity_token" "$challenge_url/internal/challenges/$challenge_id/submission-policy"
expect_status 200 -H "Authorization: Bearer $identity_token" "$identity_url/internal/users/$user_id/verify"
expect_status 200 -H "Authorization: Bearer $challenge_token" "$challenge_url/internal/challenges/$challenge_id/submission-policy"
expect_status 404 "$gateway_url/internal/users/$user_id/verify"

submission_count_before_refusals="$(db_sql "SELECT COUNT(*) FROM mindarena_submission.submission;")"
refused_email="security-refused-$(date +%s)@mindarena.local"
refused_registration="$(curl -fsS -H 'Content-Type: application/json' \
    -d "{\"fullName\":\"Refused Security User\",\"email\":\"$refused_email\",\"password\":\"smoke-password\"}" \
    "$gateway_url/api/identity/auth/register")"
refused_token="$(printf '%s' "$refused_registration" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')"
refused_user_id="$(printf '%s' "$refused_registration" | sed -n 's/.*"user":{"id":\([0-9]*\).*/\1/p')"
db_sql "DELETE FROM mindarena_identity.refresh_tokens WHERE user_id=${refused_user_id}; DELETE FROM mindarena_identity.users WHERE id=${refused_user_id};" >/dev/null
expect_status 422 -H 'Content-Type: application/json' -H "Authorization: Bearer $refused_token" \
    -d "{\"challengeId\":$challenge_id,\"content\":\"Must not persist\"}" \
    "$gateway_url/api/submissions"
expect_status 422 -H 'Content-Type: application/json' -H "Authorization: Bearer $user_token" \
    -d "{\"challengeId\":$expired_challenge_id,\"content\":\"Must not persist\"}" \
    "$gateway_url/api/submissions"

submission_count_after_refusals="$(db_sql "SELECT COUNT(*) FROM mindarena_submission.submission;")"
if [[ "$submission_count_after_refusals" != "$submission_count_before_refusals" ]]; then
    echo "A refused downstream check left partial submission data." >&2
    exit 1
fi

expect_status 200 -H 'Content-Type: application/json' -H "Authorization: Bearer $user_token" \
    -d "{\"challengeId\":$challenge_id,\"content\":\"Verified three-service submission\"}" \
    "$gateway_url/api/submissions"

printf 'Runtime service-security smoke checks passed.\n'
