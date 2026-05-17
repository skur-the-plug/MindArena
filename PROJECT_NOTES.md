# MindArena Project Notes

## Current Goal

MindArena is a Spring Boot MVC MVP for competitive social learning. Users register, join arenas, submit answers to challenges, upvote other submissions, and climb a leaderboard.

## Stack

- Java 21
- Spring Boot 3.3.5
- Spring Security
- Spring Data JPA
- Thymeleaf
- MariaDB
- Maven

## App Shape

- Entry point: `src/main/java/com/mindarena/MindArenaApplication.java`
- Config: `src/main/resources/application.properties`
- Security: `src/main/java/com/mindarena/config/SecurityConfig.java`
- Seed data: `src/main/java/com/mindarena/config/DataInitializer.java`
- Templates: `src/main/resources/templates`
- Static assets: `src/main/resources/static`

## Domain Model

- `User`: registration profile, role, skills, interests, score.
- `Arena`: competition category with name, description, and color.
- `ArenaMembership`: joins users to arenas.
- `Challenge`: arena prompt with title, brief, deadline, and active flag.
- `Submission`: user response with content and upvote count.
- `Vote`: duplicate-vote prevention between voter and submission.

## Main Flows

- Public pages: `/`, `/login`, `/register`, static CSS/JS.
- Authenticated pages: dashboard, arenas, challenges, submissions, profile, leaderboard.
- Admin-only pages: `/admin/**`.
- Registration hashes passwords with BCrypt.
- Submitting an answer awards `+10` score to the author.
- Upvoting awards `+2` score to the submission author.
- Users cannot upvote their own submission or upvote the same submission twice.

## Seed Data

`DataInitializer` creates:

- Admin account: `admin@mindarena.local` / `admin123`
- Arenas: Coding, Business, Creativity, Debate
- One default challenge per arena if no challenges exist.

## Local Run

Create a MariaDB database named `mindarena`, then run:

```bash
mvn spring-boot:run
```

Default datasource settings are:

- `DB_URL=jdbc:mariadb://localhost:3306/mindarena`
- `DB_USERNAME=mindarena`
- `DB_PASSWORD=mindarena123`

Open `http://localhost:8080`.

## Current Gaps / Next Steps

- No `src/test` directory or automated tests are present yet.
- The project directory is not currently a git repository.
- Add validation/error handling around submission and admin challenge forms.
- Consider preventing submissions after challenge deadlines.
- Consider replacing the default admin password with an environment-controlled setup for non-demo use.
- Consider adding tests for registration, arena join idempotency, scoring, and duplicate-vote prevention.
