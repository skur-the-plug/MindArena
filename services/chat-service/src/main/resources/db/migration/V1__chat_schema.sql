CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL,
    full_name VARCHAR(80),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS arena (
    id BIGINT NOT NULL,
    name VARCHAR(60),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS challenge (
    id BIGINT NOT NULL,
    title VARCHAR(120),
    arena_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_chat_challenge_arena FOREIGN KEY (arena_id) REFERENCES arena (id)
);

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_type VARCHAR(30) NOT NULL,
    author_id BIGINT NOT NULL,
    arena_id BIGINT,
    challenge_id BIGINT,
    content VARCHAR(1000) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_chat_message_author FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT fk_chat_message_arena FOREIGN KEY (arena_id) REFERENCES arena (id),
    CONSTRAINT fk_chat_message_challenge FOREIGN KEY (challenge_id) REFERENCES challenge (id)
);
