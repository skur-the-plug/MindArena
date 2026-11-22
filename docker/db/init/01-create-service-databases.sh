#!/usr/bin/env bash
set -euo pipefail

if [[ ! "${MARIADB_USER}" =~ ^[A-Za-z0-9_]+$ ]]; then
    echo "MARIADB_USER may contain only letters, numbers, and underscores" >&2
    exit 1
fi

mariadb --protocol=socket -uroot -p"${MARIADB_ROOT_PASSWORD}" <<SQL
CREATE DATABASE IF NOT EXISTS mindarena_identity CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mindarena_challenge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mindarena_ranking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mindarena_notification CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mindarena_submission CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mindarena_chat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON mindarena_identity.* TO '${MARIADB_USER}'@'%';
GRANT ALL PRIVILEGES ON mindarena_challenge.* TO '${MARIADB_USER}'@'%';
GRANT ALL PRIVILEGES ON mindarena_ranking.* TO '${MARIADB_USER}'@'%';
GRANT ALL PRIVILEGES ON mindarena_notification.* TO '${MARIADB_USER}'@'%';
GRANT ALL PRIVILEGES ON mindarena_submission.* TO '${MARIADB_USER}'@'%';
GRANT ALL PRIVILEGES ON mindarena_chat.* TO '${MARIADB_USER}'@'%';

FLUSH PRIVILEGES;
SQL
