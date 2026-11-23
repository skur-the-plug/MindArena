#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
default_output="$root_dir/../MindArena-source-2026.zip"
output_path="${1:-$default_output}"

cd "$root_dir"

if [[ -n "$(git status --porcelain --untracked-files=normal)" ]]; then
    echo "Refusing to archive a dirty worktree. Commit or remove pending files first." >&2
    exit 1
fi

mkdir -p "$(dirname "$output_path")"
git archive --format=zip --prefix=MindArena/ --output="$output_path" HEAD
unzip -tq "$output_path" >/dev/null

if unzip -Z1 "$output_path" | grep -Eq '(^|/)(\.env|target|\.idea|\.git)(/|$)'; then
    echo "Archive contains a forbidden local/generated path." >&2
    exit 1
fi

printf 'Created verified source archive: %s\n' "$output_path"
