CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL,
    full_name VARCHAR(80),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS arena (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(60) NOT NULL,
    description VARCHAR(255),
    color VARCHAR(30),
    PRIMARY KEY (id),
    UNIQUE KEY uk_challenge_arena_name (name)
);

CREATE TABLE IF NOT EXISTS challenge (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(120),
    brief VARCHAR(1200),
    arena_id BIGINT NOT NULL,
    difficulty VARCHAR(30),
    creator_id BIGINT,
    submission_template_name VARCHAR(80),
    template_type VARCHAR(60),
    submission_template_body VARCHAR(4000),
    deadline DATETIME(6),
    active BIT NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT fk_challenge_arena FOREIGN KEY (arena_id) REFERENCES arena (id),
    CONSTRAINT fk_challenge_creator FOREIGN KEY (creator_id) REFERENCES users (id)
);
