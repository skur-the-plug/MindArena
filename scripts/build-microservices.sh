#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

modules=(
  services/discovery-server
  services/config-server
  services/api-gateway
  services/identity-service
  services/challenge-service
  services/ranking-service
  services/notification-service
  services/submission-service
  services/chat-service
)

for module in "${modules[@]}"; do
  printf 'Building %s\n' "$module"
  (cd "$ROOT_DIR/$module" && mvn -q test)
done

printf 'Microservices build passed.\n'
