# Microservices Architecture

MindArena now has a service-first Spring Cloud runtime. The default Docker Compose stack starts
the gateway and extracted domain services only; the legacy MVC application is kept behind an
optional `legacy-ui` profile for demos that still need the server-rendered screens.

## Runtime Services

- `discovery`: Eureka service registry on port `8761`.
- `config`: Spring Cloud Config Server on port `8888`, using a native classpath config backend.
- `gateway`: Spring Cloud Gateway on port `8082`.
- `identity`: Extracted identity/profile API service registered as `mindarena-identity-service`.
- `challenge`: Extracted arena/challenge API service registered as `mindarena-challenge-service`.
- `ranking`: Extracted leaderboard API service registered as `mindarena-ranking-service`.
- `notification`: Extracted notification API service registered as `mindarena-notification-service`.
- `submission`: Extracted submission/comment/vote API service registered as `mindarena-submission-service`.
- `chat`: Extracted chat-message API service registered as `mindarena-chat-service`.
- `db`: MariaDB. Extracted services use service-owned schemas
  such as `mindarena_identity`, `mindarena_challenge`, `mindarena_ranking`,
  `mindarena_notification`, `mindarena_submission`, and `mindarena_chat`. The optional legacy UI
  keeps its old `mindarena` schema.
- `redis`: Redis leaderboard cache.
- `rabbitmq`: Domain event broker.
- `app`: Optional legacy Thymeleaf UI, registered as `mindarena-app` only when the
  `legacy-ui` Docker Compose profile is enabled.

## Target Boundaries

- `identity`: authentication, users, profiles, roles, privileges.
- `challenges`: arenas, challenges, judges, platform news.
- `rankings`: leaderboards, rank progression, leaderboard events.
- `notifications`: user notifications and XP events.
- `chat`: chat rooms, messages, WebSocket updates.
- `submissions`: submissions, comments, votes, submission templates.

## Runtime Contract

1. The gateway is the backend entry point for client and automation traffic.
2. Authentication is owned by `identity`. Clients call `/api/identity/auth/register` or
   `/api/identity/auth/login` to receive a short-lived RS256 access token and an opaque rotating
   refresh token. Clients send `Authorization: Bearer <token>` on protected `/api/**` requests
   and use `/api/identity/auth/refresh` to rotate refresh tokens and obtain new access tokens.
3. The gateway verifies access tokens with the identity public key and forwards trusted user
   context to services with `X-User-Id`, `X-User-Email`, and `X-User-Role`. Domain services use
   those headers for current-user and role checks instead of trusting user IDs from request bodies
   or query parameters.
4. Business APIs are routed through the gateway:
   `/api/identity/**`, `/api/challenges/**`, `/api/leaderboards/**`, `/api/notifications/**`,
   `/api/submissions/**`, and `/api/chat/**`.
5. Each extracted service owns a separate local MariaDB schema in Docker Compose. Flyway creates
   service tables, and `docker/db/backfill-service-schemas.sql` can copy demo data from the
   monolith schema. The local Compose database uses one application user for developer
   convenience; production should use separate least-privilege database users per service.
6. Some services keep local read/projection tables for user, arena, challenge, or submission
   data that they need to answer their own API requests. In this MVP those projections are
   populated by the demo backfill script. Full production synchronization should be implemented
   with domain events or service APIs before presenting the split as independently consistent.
7. RabbitMQ support exists for asynchronous platform events, but the extracted services should be
   described as a service-first MVP rather than a complete event-driven system.
8. The default gateway configuration disables service-discovery route exposure and has no catch-all
   route to the legacy MVC app. Only explicit `/api/**` routes are exposed by the gateway. The
   catch-all route exists only in the gateway `legacy-ui` Spring profile. The legacy UI keeps its
   form-login session flow for demos only; it is not the auth boundary for service APIs.

## Local URLs

- Gateway: `http://localhost:8082`
- Config server: `http://localhost:8888`
- Eureka dashboard: `http://localhost:8761`
- RabbitMQ management: `http://localhost:15672`
- Legacy UI direct, optional profile only: `http://localhost:8081`

## Kubernetes

The `k8s/base` manifests provide a local/dev Kubernetes runtime for the microservices backend.
They include:

- Namespace, shared ConfigMap, and local-development Secret.
- Deployments and Services for gateway, discovery, config, identity, challenge, ranking,
  notification, submission, and chat.
- MariaDB, Redis, and RabbitMQ with PVC-backed storage.
- Readiness probes and resource requests/limits for the application services.

Production hardening still needs environment-specific overlays, managed secrets, ingress/TLS,
autoscaling, network policies, image registry publishing, centralized logs, metrics, tracing,
and preferably managed infrastructure for databases and brokers.
