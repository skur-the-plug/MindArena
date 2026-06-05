# MindArena DevOps Guide

This guide describes the MindArena MVP DevOps setup. The target environment is development/staging for an academic project. Production would still require managed databases, external secret management, ingress/TLS, backup automation, stronger observability, and formal release governance.

## CI Pipeline

GitHub Actions workflow: `.github/workflows/ci.yml`.

The `test-and-quality` job runs on pushes and pull requests:

1. Checkout with full history for analysis tools.
2. Set up Java 21 with Maven cache.
3. Run `mvn -B verify` for the root app and JaCoCo coverage.
4. Run `bash scripts/build-microservices.sh` to test each service module.
5. Run SonarCloud analysis when `SONAR_TOKEN` is configured.
6. Prepare security report output.
7. Run OWASP Dependency Check when `NVD_API_KEY` is configured.
8. Otherwise run Trivy filesystem dependency scanning and upload SARIF.
9. Upload dependency scan SARIF to GitHub code scanning when a report exists.
10. Run Gitleaks secret scanning.

Recommended GitHub secrets:

```text
SONAR_TOKEN
NVD_API_KEY
```

`NVD_API_KEY` is recommended because unauthenticated NVD API updates are rate-limited. Without it, the workflow uses Trivy as a reliable fallback and reports findings without blocking CI.

## Dependabot

Dependabot is configured in `.github/dependabot.yml` for:

- Maven dependencies in the root app and all service modules.
- GitHub Actions.
- Docker Compose.
- Dockerfiles.

Update PRs should be reviewed, tested through CI, and merged only when the build and scans pass.

## Docker Compose

Local runtime file: `docker-compose.yml`.

Before running:

```bash
cp .env.example .env
```

Set real local values for:

```text
MARIADB_USER
MARIADB_PASSWORD
MARIADB_ROOT_PASSWORD
RABBITMQ_DEFAULT_USER
RABBITMQ_DEFAULT_PASS
GRAFANA_ADMIN_USER
GRAFANA_ADMIN_PASSWORD
```

Start default microservices:

```bash
docker compose up --build
```

Start monitoring:

```bash
docker compose --profile monitoring up --build
```

Start the optional legacy UI:

```bash
docker compose --profile legacy-ui up --build
```

Backfill service schemas from the legacy schema when needed:

```bash
docker compose exec -T db sh -c 'mariadb -uroot -p"$MARIADB_ROOT_PASSWORD"' < docker/db/backfill-service-schemas.sql
```

## Kubernetes

Kubernetes manifests live in `k8s/base`.

Build local images into your cluster image store, then apply:

```bash
kubectl apply -f k8s/base/namespace.yaml
kubectl create secret generic mindarena-secrets \
  --namespace mindarena \
  --from-literal=db-username="$DB_USERNAME" \
  --from-literal=db-password="$DB_PASSWORD" \
  --from-literal=db-root-password="$DB_ROOT_PASSWORD" \
  --from-literal=rabbitmq-username="$RABBITMQ_USERNAME" \
  --from-literal=rabbitmq-password="$RABBITMQ_PASSWORD" \
  --from-literal=grafana-admin-user="$GRAFANA_ADMIN_USER" \
  --from-literal=grafana-admin-password="$GRAFANA_ADMIN_PASSWORD"

kubectl apply -f k8s/base/config.yaml
kubectl apply -f k8s/base/security.yaml
kubectl apply -f k8s/base/infra.yaml
kubectl apply -f k8s/base/spring-cloud.yaml
kubectl apply -f k8s/base/domain-services.yaml
kubectl apply -f k8s/base/monitoring.yaml
```

Watch rollout:

```bash
kubectl get pods -n mindarena -w
kubectl get deploy,svc,hpa,pdb -n mindarena
```

Open the gateway:

```bash
kubectl port-forward -n mindarena service/gateway 8082:8080
```

## Monitoring

Spring services expose:

```text
/actuator/health
/actuator/prometheus
```

Compose monitoring:

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

Kubernetes monitoring:

```bash
kubectl port-forward -n mindarena service/prometheus 9090:9090
kubectl port-forward -n mindarena service/grafana 3000:3000
```

Grafana uses the Prometheus datasource provisioned in the repository. Compose also includes a basic MindArena overview dashboard.

## Smoke Tests

Smoke-test script:

```bash
BASE_URL=http://localhost:8082 bash scripts/smoke-step4.sh
```

Use it after Compose startup, Kubernetes port-forwarding, or a release deployment. The script validates that the gateway can reach the core service routes.

## Rollback

For this MVP, rollback is based on Git tags and image tags.

Release tag:

```text
v0.1.0
```

Rollback steps for Compose:

```bash
git fetch --tags
git checkout v0.1.0
cp .env.example .env
# restore real local secret values
docker compose down
docker compose up --build
BASE_URL=http://localhost:8082 bash scripts/smoke-step4.sh
```

Rollback steps for Kubernetes:

```bash
git fetch --tags
git checkout v0.1.0
# rebuild or pull images tagged 0.1.0
kubectl apply -f k8s/base/config.yaml
kubectl apply -f k8s/base/security.yaml
kubectl apply -f k8s/base/infra.yaml
kubectl apply -f k8s/base/spring-cloud.yaml
kubectl apply -f k8s/base/domain-services.yaml
kubectl apply -f k8s/base/monitoring.yaml
kubectl rollout status deployment/gateway -n mindarena
BASE_URL=http://localhost:8082 bash scripts/smoke-step4.sh
```

If a newer deployment is already running and only one service is bad, use:

```bash
kubectl rollout undo deployment/<deployment-name> -n mindarena
kubectl rollout status deployment/<deployment-name> -n mindarena
```

Rollback acceptance criteria:

- Pods become ready.
- Gateway route smoke tests pass.
- Prometheus targets return `up == 1`.
- No new critical errors appear in service logs.
