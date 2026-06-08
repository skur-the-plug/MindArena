CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_identity_refresh_tokens_hash (token_hash),
    KEY idx_identity_refresh_tokens_user (user_id),
    CONSTRAINT fk_identity_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);
