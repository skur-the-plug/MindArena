REPLACE INTO mindarena_identity.users (id, full_name, email, password, skills, interests, profile_image_url, role, score, created_at)
SELECT id, full_name, email, password, skills, interests, profile_image_url, role, score, created_at
FROM mindarena.users;

REPLACE INTO mindarena_challenge.users (id, full_name)
SELECT id, full_name FROM mindarena.users;

REPLACE INTO mindarena_challenge.arena (id, name, description, color)
SELECT id, name, description, color FROM mindarena.arena;

ALTER TABLE mindarena_challenge.challenge
    MODIFY template_type VARCHAR(60);

REPLACE INTO mindarena_challenge.challenge (
    id, title, brief, arena_id, difficulty, creator_id, submission_template_name,
    template_type, submission_template_body, deadline, active
)
SELECT id, title, brief, arena_id, difficulty, creator_id, submission_template_name,
       template_type, submission_template_body, deadline, active
FROM mindarena.challenge;

REPLACE INTO mindarena_ranking.users (id, full_name, role, score)
SELECT id, full_name, role, score FROM mindarena.users;

REPLACE INTO mindarena_ranking.arena (id, name)
SELECT id, name FROM mindarena.arena;

REPLACE INTO mindarena_ranking.challenge (id, title, difficulty, arena_id)
SELECT id, title, difficulty, arena_id FROM mindarena.challenge;

REPLACE INTO mindarena_ranking.submission (id, challenge_id, author_id, upvotes, best_answer)
SELECT id, challenge_id, author_id, upvotes, best_answer FROM mindarena.submission;

REPLACE INTO mindarena_notification.users (id, full_name)
SELECT id, full_name FROM mindarena.users;

REPLACE INTO mindarena_notification.user_notification (
    id, recipient_id, type, message, link_url, read_flag, created_at
)
SELECT id, recipient_id, type, message, link_url, read_flag, created_at
FROM mindarena.user_notification;

REPLACE INTO mindarena_submission.users (id, full_name)
SELECT id, full_name FROM mindarena.users;

REPLACE INTO mindarena_submission.arena (id, name)
SELECT id, name FROM mindarena.arena;

REPLACE INTO mindarena_submission.challenge (id, title, difficulty, arena_id)
SELECT id, title, difficulty, arena_id FROM mindarena.challenge;

REPLACE INTO mindarena_submission.submission (
    id, challenge_id, author_id, content, content_json, upvotes, best_answer, created_at
)
SELECT id, challenge_id, author_id, content, content_json, upvotes, best_answer, created_at
FROM mindarena.submission;

REPLACE INTO mindarena_submission.submission_comment (
    id, submission_id, author_id, content, created_at
)
SELECT id, submission_id, author_id, content, created_at
FROM mindarena.submission_comment;

INSERT IGNORE INTO mindarena_submission.vote (id, voter_id, submission_id, created_at)
SELECT id, voter_id, submission_id, created_at
FROM mindarena.vote;

REPLACE INTO mindarena_chat.users (id, full_name)
SELECT id, full_name FROM mindarena.users;

REPLACE INTO mindarena_chat.arena (id, name)
SELECT id, name FROM mindarena.arena;

REPLACE INTO mindarena_chat.challenge (id, title, arena_id)
SELECT id, title, arena_id FROM mindarena.challenge;

REPLACE INTO mindarena_chat.chat_message (
    id, room_type, author_id, arena_id, challenge_id, content, created_at
)
SELECT id, room_type, author_id, arena_id, challenge_id, content, created_at
FROM mindarena.chat_message;
