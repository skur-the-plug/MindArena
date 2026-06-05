CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL,
    full_name VARCHAR(80),
    role VARCHAR(20) NOT NULL,
    score INT NOT NULL DEFAULT 0,
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
    difficulty VARCHAR(30),
    arena_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ranking_challenge_arena FOREIGN KEY (arena_id) REFERENCES arena (id)
);

CREATE TABLE IF NOT EXISTS submission (
    id BIGINT NOT NULL,
    challenge_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    upvotes INT NOT NULL DEFAULT 0,
    best_answer BIT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_ranking_submission_challenge FOREIGN KEY (challenge_id) REFERENCES challenge (id),
    CONSTRAINT fk_ranking_submission_author FOREIGN KEY (author_id) REFERENCES users (id)
);
