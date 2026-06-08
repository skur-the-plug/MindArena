#!/usr/bin/env bash
set -euo pipefail

tmpdir="$(mktemp -d)"
trap 'rm -rf "$tmpdir"' EXIT

openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$tmpdir/private.pem" >/dev/null 2>&1
openssl rsa -in "$tmpdir/private.pem" -pubout -out "$tmpdir/public.pem" >/dev/null 2>&1

private_key="$(sed '/-----/d' "$tmpdir/private.pem" | tr -d '\n')"
public_key="$(sed '/-----/d' "$tmpdir/public.pem" | tr -d '\n')"

printf 'JWT_PRIVATE_KEY=%s\n' "$private_key"
printf 'JWT_PUBLIC_KEY=%s\n' "$public_key"
