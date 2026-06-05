# MindArena

MindArena is a Spring Boot and Spring Cloud MVP for competitive social
learning. Users join arenas, solve challenges, submit structured answers,
vote on peer work, receive notifications, chat around arenas/challenges,
and climb leaderboards.

This repository is structured as a development/staging DevOps platform
for an academic MVP. It is not presented as an enterprise production
platform.

## Architecture

The default runtime is a Spring Cloud microservices backend:

- `services/api-gateway`: Spring Cloud Gateway entry point on port `8080`
  in containers, published locally as `8082`.
- `services/discovery-server`: Eureka service registry on port `8761`.
- `services/config-server`: Spring Cloud Config Server on port `8888`.
- `services/identity-service`: registration, profiles, and identity API.
- `services/challenge-service`: arenas and challenge catalog API.
- `services/ranking-service`: leaderboard and rank API with Redis support.
- `services/notification-service`: user notification API.
- `services/submission-service`: submissions, comments, and votes API.
- `services/chat-service`: chat messages and rooms API.

Supporting infrastructure:

- MariaDB for service schemas.
- Redis for cache/ranking support.
- RabbitMQ for asynchronous messaging.
- Prometheus and Grafana for local monitoring.

The root Spring Boot application is retained as an optional legacy
Thymeleaf UI/demo app under the `legacy-ui` Docker Compose profile.

Text architecture diagram:

```text
Browser / API client
        |
        v
Spring Cloud Gateway (:8082 locally)
        |
        +--> Identity Service
        +--> Challenge Service
        +--> Ranking Service ----> Redis
        +--> Notification Service
        +--> Submission Service
        +--> Chat Service
        |
        +--> Eureka Discovery
        +--> Config Server

Domain services ----> MariaDB service schemas
Domain events  ----> RabbitMQ
Metrics        ----> Prometheus ----> Grafana
```

## DevOps And Platform Practices

Implemented:

- GitHub Actions CI for Maven verification and microservice builds.
- JaCoCo coverage report generation.
- Conditional SonarCloud analysis when `SONAR_TOKEN` is configured.
- Dependency vulnerability scanning through OWASP Dependency Check when
  `NVD_API_KEY` is configured, with Trivy SARIF fallback otherwise.
- Gitleaks secret scanning.
- Dependabot for GitHub Actions, Maven, Dockerfiles, and Docker Compose.
- Docker Compose local runtime with required environment-based secrets.
- Kubernetes base manifests for local/dev clusters.
- Prometheus/Grafana monitoring for Compose and Kubernetes.
- Smoke-test script for deployed gateway routes.
- Non-root application containers and hardened Kubernetes base settings.

This is intentionally a development/staging DevOps MVP. Clean production
hardening is still required before using this as a real production
platform.

See [DEVOPS.md](DEVOPS.md) for deployment, CI, monitoring, smoke-test,
and rollback details.

## Repository Layout

```text
.github/workflows/ci.yml      CI pipeline
.github/dependabot.yml        Dependency update automation
docker-compose.yml            Local microservices runtime
docker/                       DB init, Prometheus, and Grafana config
k8s/base/                     Local/dev Kubernetes manifests
scripts/build-microservices.sh
scripts/smoke-step4.sh
services/                     Spring Cloud services
src/                          Optional legacy UI/root Spring Boot app
```

## Requirements

- Java 21
- Maven 3.9+
- Docker and Docker Compose
- Optional: Kind, Minikube, or Docker Desktop Kubernetes
- Optional: GitHub secrets `SONAR_TOKEN` and `NVD_API_KEY`

## Run Locally With Docker Compose

Create a local environment file:

```bash
cp .env.example .env
```

Edit `.env` and replace every placeholder value. Then start the default
microservices runtime:

```bash
docker compose up --build
```

Main local endpoints:

- Gateway: `http://localhost:8082`
- Eureka dashboard: `http://localhost:8761`
- Config server: `http://localhost:8888`
- RabbitMQ management: `http://localhost:15672`

Gateway routes:

- `/api/identity/**`
- `/api/challenges/**`
- `/api/leaderboards/**`
- `/api/notifications/**`
- `/api/submissions/**`
- `/api/chat/**`

Start monitoring:

```bash
docker compose --profile monitoring up --build
```

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

Run the optional legacy UI:

```bash
docker compose --profile legacy-ui up --build
```

Legacy UI endpoint: `http://localhost:8081`

## Test And Build

Run root app tests and coverage:

```bash
mvn -B verify
```

Build and test all microservices:

```bash
bash scripts/build-microservices.sh
```

Run smoke checks against a running gateway:

```bash
BASE_URL=http://localhost:8082 bash scripts/smoke-step4.sh
```

## Kubernetes

The Kubernetes base in `k8s/base` targets local/dev clusters such as
Minikube, Kind, or Docker Desktop Kubernetes. It includes:

- Namespace with restricted pod security labels.
- Runtime ConfigMap and externalized Secret template.
- MariaDB, Redis, RabbitMQ.
- Discovery, config server, gateway, and domain services.
- Prometheus and Grafana.
- Network policies, resource quota, limit range, HPAs, PDBs, and
  non-privileged container settings.

Read [k8s/base/README.md](k8s/base/README.md) before applying manifests.

## Secrets

Real secrets are not committed. Local Compose uses `.env`; Kubernetes
uses `mindarena-secrets`, created at deploy time.

Demo admin seeding is disabled by default. To enable it locally:

```bash
export DEMO_ADMIN_ENABLED=true
export DEMO_ADMIN_EMAIL=admin@mindarena.local
export DEMO_ADMIN_PASSWORD=replace-with-demo-admin-password
```

## Release

Current MVP release: `v0.1.0`.

Release and rollback notes are maintained in GitHub Releases and
summarized in [DEVOPS.md](DEVOPS.md).

Rollback summary:

- Compose rollback: check out tag `v0.1.0`, restore `.env`, rebuild, and
  run the gateway smoke test.
- Kubernetes rollback: check out tag `v0.1.0`, rebuild or pull `0.1.0`
  images, reapply `k8s/base`, or use `kubectl rollout undo` for one bad
  deployment.
- Rollback is accepted only when pods are ready, smoke tests pass,
  Prometheus targets are up, and critical service logs are clean.

Production hardening still required:

- External secret management instead of local `.env` or manually created
  Kubernetes secrets.
- Managed databases or database backup/restore automation.
- Ingress, TLS, and production DNS.
- Centralized logs and distributed tracing.
- Signed images and admission policies.
- Stronger test coverage and release promotion gates.
