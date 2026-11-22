#!/usr/bin/env bash
set -euo pipefail

tmpdir="$(mktemp -d)"
trap 'rm -rf "$tmpdir"' EXIT

openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$tmpdir/private.pem" >/dev/null 2>&1
openssl rsa -in "$tmpdir/private.pem" -pubout -out "$tmpdir/public.pem" >/dev/null 2>&1

private_key="$(sed '/-----/d' "$tmpdir/private.pem" | tr -d '\n')"
public_key="$(sed '/-----/d' "$tmpdir/public.pem" | tr -d '\n')"

if [[ "${1:-}" == "--write-env" ]]; then
    env_file="${2:-.env}"
    if [[ ! -f "$env_file" ]]; then
        echo "Environment file not found: $env_file" >&2
        exit 1
    fi

    updated_env="$(mktemp)"
    trap 'rm -rf "$tmpdir" "$updated_env"' EXIT
    awk -v private_key="$private_key" -v public_key="$public_key" '
        BEGIN { private_written = 0; public_written = 0 }
        /^JWT_PRIVATE_KEY=/ {
            print "JWT_PRIVATE_KEY=" private_key
            private_written = 1
            next
        }
        /^JWT_PUBLIC_KEY=/ {
            print "JWT_PUBLIC_KEY=" public_key
            public_written = 1
            next
        }
        { print }
        END {
            if (!private_written) print "JWT_PRIVATE_KEY=" private_key
            if (!public_written) print "JWT_PUBLIC_KEY=" public_key
        }
    ' "$env_file" > "$updated_env"
    chmod --reference="$env_file" "$updated_env"
    mv "$updated_env" "$env_file"
    echo "Rotated local JWT keys in $env_file (key material not printed)."
elif [[ "${1:-}" == "--print" ]]; then
    printf 'JWT_PRIVATE_KEY=%s\n' "$private_key"
    printf 'JWT_PUBLIC_KEY=%s\n' "$public_key"
else
    echo "Usage: $0 --write-env [path] | --print" >&2
    echo "Use --write-env to avoid exposing private key material in terminal history." >&2
    exit 2
fi
