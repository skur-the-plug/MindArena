CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(80),
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    skills VARCHAR(255),
    interests VARCHAR(255),
    profile_image_url VARCHAR(500),
    role VARCHAR(20) NOT NULL,
    score INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_identity_users_email (email)
);
