CREATE DATABASE IF NOT EXISTS mindarena_identity CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mindarena_challenge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mindarena_ranking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mindarena_notification CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mindarena_submission CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mindarena_chat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON mindarena_identity.* TO 'mindarena'@'%';
GRANT ALL PRIVILEGES ON mindarena_challenge.* TO 'mindarena'@'%';
GRANT ALL PRIVILEGES ON mindarena_ranking.* TO 'mindarena'@'%';
GRANT ALL PRIVILEGES ON mindarena_notification.* TO 'mindarena'@'%';
GRANT ALL PRIVILEGES ON mindarena_submission.* TO 'mindarena'@'%';
GRANT ALL PRIVILEGES ON mindarena_chat.* TO 'mindarena'@'%';

FLUSH PRIVILEGES;
