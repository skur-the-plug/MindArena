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
    difficulty VARCHAR(30),
    arena_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_submission_challenge_arena FOREIGN KEY (arena_id) REFERENCES arena (id)
);

CREATE TABLE IF NOT EXISTS submission (
    id BIGINT NOT NULL AUTO_INCREMENT,
    challenge_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content VARCHAR(4000),
    content_json VARCHAR(12000),
    upvotes INT NOT NULL DEFAULT 0,
    best_answer BIT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_submission_challenge FOREIGN KEY (challenge_id) REFERENCES challenge (id),
    CONSTRAINT fk_submission_author FOREIGN KEY (author_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS submission_comment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_submission_comment_submission FOREIGN KEY (submission_id) REFERENCES submission (id),
    CONSTRAINT fk_submission_comment_author FOREIGN KEY (author_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS vote (
    id BIGINT NOT NULL AUTO_INCREMENT,
    voter_id BIGINT NOT NULL,
    submission_id BIGINT NOT NULL,
    created_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_vote_voter_submission (voter_id, submission_id),
    CONSTRAINT fk_vote_voter FOREIGN KEY (voter_id) REFERENCES users (id),
    CONSTRAINT fk_vote_submission FOREIGN KEY (submission_id) REFERENCES submission (id)
);
