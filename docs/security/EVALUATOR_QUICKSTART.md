# MindArena — Evaluator Quick Start

This guide demonstrates the assessed contribution: authenticated and authorized
communication among **Submission**, **Identity**, and **Challenge** services.

## 1. Prerequisites

- Java 21
- Maven 3.9 or newer
- Docker with Docker Compose v2
- OpenSSL and `curl`

For IntelliJ IDEA, open the extracted `MindArena` directory, select Java 21 as
the project SDK, and allow IntelliJ to import the root and `services/*/pom.xml`
Maven projects. No `.idea` files or machine-specific paths are required.

## 2. Create local secrets

From the repository root:

```bash
cp .env.example .env
```

Replace the database, RabbitMQ, and Grafana placeholder passwords in `.env`,
then generate disposable local keys and service credentials:

```bash
bash scripts/generate-jwt-keys.sh --write-env .env
bash scripts/generate-service-credentials.sh --write-env .env
```

The scripts write secrets directly to the ignored `.env` file and do not print
private material. Never commit `.env`.

## 3. Fast verification

```bash
bash scripts/test-service-security.sh
bash scripts/build-microservices.sh
mvn -B verify
```

## 4. Reproducible runtime proof

The test override publishes the three assessed services on loopback only. It is
intended for local verification, not deployment.

```bash
docker compose -p mindarena-security-qa \
  -f docker-compose.yml \
  -f docker-compose.security-test.yml \
  up -d --build

COMPOSE_PROJECT_NAME=mindarena-security-qa \
  bash scripts/smoke-service-security.sh

docker compose -p mindarena-security-qa \
  -f docker-compose.yml \
  -f docker-compose.security-test.yml \
  down -v
```

The smoke test proves the successful three-service request and rejects missing,
malformed, user, wrong-audience, and header-spoofed authority. It also verifies
that independent Identity and Challenge refusals leave no partial submission.

Expected final line:

```text
Runtime service-security smoke checks passed.
```

## 5. What to inspect

- `docs/security/service-to-service-security.md`: design and threat model.
- `docs/report/ensias/rapport-mindarena.tex`: detailed ENSIAS LaTeX report source.
- `docs/report/VALIDATION_EVIDENCE.md`: exact tests and result ledger.
- `services/identity-service/.../security/`: issuer-side client registry and policy.
- `services/challenge-service/.../security/`: Challenge resource-server policy.
- `services/submission-service/.../client/`: service-token cache and clients.
- `services/submission-service/.../service/SubmissionApiService.java`: atomic flow.
- `scripts/smoke-service-security.sh`: executable positive and negative proof.

## Security scope

The assessed implementation secures the Submission write flow. It does not claim
that every legacy MindArena endpoint has been converted to service identity.
