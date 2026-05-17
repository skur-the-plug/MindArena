# MindArena

MindArena is a Spring Boot + MariaDB MVP for competitive social learning. Users join arenas, answer challenges, vote on submissions, and climb arena leaderboards.

## MVP Features

- User registration and login
- Profile with skills and interests
- Arena browsing and joining
- Challenge browsing by arena
- Text submissions
- Upvote system with duplicate-vote prevention
- Arena leaderboard
- Admin pages for challenge creation and moderation

## Stack

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- Thymeleaf
- MariaDB
- HTML/CSS/JavaScript

## Run Locally

Create a MariaDB database:

```sql
CREATE DATABASE mindarena CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Set credentials if they differ from the defaults:

```bash
export DB_URL=jdbc:mariadb://localhost:3306/mindarena
export DB_USERNAME=mindarena
export DB_PASSWORD=mindarena123
```

Run:

```bash
mvn spring-boot:run
```

Open `http://localhost:8080`.

Demo admin account:

- Email: `admin@mindarena.local`
- Password: `admin123`
