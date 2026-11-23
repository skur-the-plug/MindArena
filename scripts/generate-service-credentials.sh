#!/usr/bin/env bash
set -euo pipefail

if [[ "${1:-}" != "--write-env" ]]; then
    echo "Usage: $0 --write-env [path]" >&2
    echo "The generated service secret is written directly to an ignored environment file." >&2
    exit 2
fi

env_file="${2:-.env}"
if [[ ! -f "$env_file" ]]; then
    echo "Environment file not found: $env_file" >&2
    exit 1
fi

client_id="mindarena-submission-service"
client_secret="$(openssl rand -hex 32)"
secret_hash="$(printf '%s' "$client_secret" | sha256sum | cut -d ' ' -f 1)"
updated_env="$(mktemp)"
trap 'rm -f "$updated_env"' EXIT

awk -v client_id="$client_id" -v client_secret="$client_secret" -v secret_hash="$secret_hash" '
    BEGIN { id_written = 0; secret_written = 0; hash_written = 0 }
    /^SUBMISSION_SERVICE_CLIENT_ID=/ {
        print "SUBMISSION_SERVICE_CLIENT_ID=" client_id
        id_written = 1
        next
    }
    /^SUBMISSION_SERVICE_CLIENT_SECRET=/ {
        print "SUBMISSION_SERVICE_CLIENT_SECRET=" client_secret
        secret_written = 1
        next
    }
    /^SUBMISSION_SERVICE_CLIENT_SECRET_SHA256=/ {
        print "SUBMISSION_SERVICE_CLIENT_SECRET_SHA256=" secret_hash
        hash_written = 1
        next
    }
    { print }
    END {
        if (!id_written) print "SUBMISSION_SERVICE_CLIENT_ID=" client_id
        if (!secret_written) print "SUBMISSION_SERVICE_CLIENT_SECRET=" client_secret
        if (!hash_written) print "SUBMISSION_SERVICE_CLIENT_SECRET_SHA256=" secret_hash
    }
' "$env_file" > "$updated_env"

chmod --reference="$env_file" "$updated_env"
mv "$updated_env" "$env_file"
trap - EXIT
echo "Rotated Submission service credentials in $env_file (secret material not printed)."
