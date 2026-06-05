# MindArena

MindArena is a Spring Boot + Spring Cloud microservices MVP for competitive social learning. Users join themed arenas, complete challenge missions, submit structured artifacts, vote on peer submissions, and climb prestige leaderboards.

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
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka
- Spring Cloud Config
- Spring Security
- Spring Data JPA
- Flyway
- MariaDB
- Redis
- RabbitMQ
- Docker Compose

The optional legacy UI uses Thymeleaf, HTML, CSS, and JavaScript.

## Run Locally

The default local setup starts the microservices backend only:

```bash
cp .env.example .env
# edit .env and set real local credentials
docker compose up --build
```

Open the gateway at `http://localhost:8082`.

The Spring Cloud runtime exposes:

- Gateway: `http://localhost:8082`
- Identity service: `mindarena-identity-service` behind the gateway at `/api/identity`
- Challenge service: `mindarena-challenge-service` behind the gateway at `/api/challenges`
- Ranking service: `mindarena-ranking-service` behind the gateway at `/api/leaderboards`
- Notification service: `mindarena-notification-service` behind the gateway at `/api/notifications`
- Submission service: `mindarena-submission-service` behind the gateway at `/api/submissions`
- Chat service: `mindarena-chat-service` behind the gateway at `/api/chat`
- Config server: `http://localhost:8888`
- Eureka dashboard: `http://localhost:8761`
- RabbitMQ management: `http://localhost:15672`

The microservices runtime uses separate MariaDB schemas:
`mindarena_identity`, `mindarena_challenge`, `mindarena_ranking`, `mindarena_notification`,
`mindarena_submission`, and `mindarena_chat`.

The old Thymeleaf MVC application is kept as an optional legacy/demo UI, outside the default
microservices runtime:

```bash
docker compose --profile legacy-ui up --build
```

When that profile is enabled, the legacy UI is available directly at `http://localhost:8081`.
If you intentionally want the gateway to proxy the legacy UI, also start the gateway with the
`legacy-ui` Spring profile.

To backfill service schemas from the monolith schema after the stack is up:

```bash
docker compose exec -T db sh -c 'mariadb -uroot -p"$MARIADB_ROOT_PASSWORD"' < docker/db/backfill-service-schemas.sql
```

Prometheus and Grafana are available through the monitoring profile:

```bash
docker compose --profile monitoring up --build
```

Prometheus runs at `http://localhost:9090`; Grafana runs at `http://localhost:3000`.

Run microservices route smoke checks:

```bash
BASE_URL=http://localhost:8082 bash scripts/smoke-step4.sh
```

## Run On Kubernetes

Kubernetes manifests are available in `k8s/base` for a local/dev cluster. They deploy the same
microservices backend with a gateway, discovery server, config server, MariaDB, Redis, RabbitMQ,
and the extracted domain services.

See `k8s/base/README.md` for image build and deployment commands.

To run against a manually managed database, create a MariaDB database:

```sql
CREATE DATABASE mindarena CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Set credentials with environment variables:

```bash
export DB_URL=jdbc:mariadb://localhost:3306/mindarena
export DB_USERNAME=replace-with-db-user
export DB_PASSWORD=replace-with-db-password
```

Run:

```bash
mvn spring-boot:run
```

Open `http://localhost:8080`.

Demo admin seeding is disabled by default. To create a local demo admin, set:

```bash
export DEMO_ADMIN_ENABLED=true
export DEMO_ADMIN_EMAIL=admin@mindarena.local
export DEMO_ADMIN_PASSWORD=replace-with-demo-admin-password
```
