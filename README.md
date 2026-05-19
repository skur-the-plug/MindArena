# MindArena

MindArena is a Spring Boot + MariaDB MVP for competitive social learning. Users join themed arenas, complete challenge missions, submit structured artifacts, vote on peer submissions, and climb prestige leaderboards.

The current frontend uses a dark "Cyber Arena" interface with a mission hub, arena zones, challenge briefing pages, chat entry points, and leaderboard views.

## MVP Features

- User registration and login
- Profile with skills and interests
- Arena browsing and joining
- Challenge browsing by arena
- Structured submission templates for coding, business, creativity, and debate challenges
- Upvote system with duplicate-vote prevention
- Global, arena, challenge, and rank leaderboards
- Chat rooms for global, arena, and challenge discussion
- Real-time chat updates over WebSockets, with the existing form submit as a fallback
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

The fastest full local setup is Docker Compose:

```bash
docker compose up --build
```

Open `http://localhost:8081`.

The Spring Cloud runtime also exposes:

- Gateway: `http://localhost:8082`
- Eureka dashboard: `http://localhost:8761`
- RabbitMQ management: `http://localhost:15672`

To run against a manually managed database, create a MariaDB database:

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
