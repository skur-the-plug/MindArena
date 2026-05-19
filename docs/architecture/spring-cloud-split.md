# Spring Cloud Split

MindArena is still one deployable application for the main product UI, but the code now has domain
boundaries and a Spring Cloud runtime layer for gradual service extraction.

## Runtime Services

- `discovery`: Eureka service registry on port `8761`.
- `gateway`: Spring Cloud Gateway on port `8082`.
- `app`: Current MindArena application, registered in Eureka as `mindarena-app`.
- `db`: MariaDB.
- `redis`: Redis leaderboard cache.
- `rabbitmq`: Domain event broker.

## Target Boundaries

- `identity`: authentication, users, profiles, roles, privileges.
- `challenges`: arenas, challenges, judges, platform news.
- `rankings`: leaderboards, rank progression, leaderboard events.
- `notifications`: user notifications and XP events.
- `chat`: chat rooms, messages, WebSocket updates.
- `submissions`: submissions, comments, votes, submission templates.

## Extraction Order

1. Keep the monolith registered as `mindarena-app` behind the gateway.
2. Extract read-heavy APIs first, starting with rankings, because they already have Redis caching
   and broker-backed invalidation.
3. Extract identity only after session/auth responsibilities are defined at the gateway boundary.
4. Move each extracted service to its own database ownership model instead of sharing JPA entities
   across services.
5. Keep RabbitMQ domain events as the integration contract for cache invalidation, notifications,
   XP changes, and future asynchronous evaluation.

## Local URLs

- App direct: `http://localhost:8081`
- Gateway: `http://localhost:8082`
- Eureka dashboard: `http://localhost:8761`
- RabbitMQ management: `http://localhost:15672`
