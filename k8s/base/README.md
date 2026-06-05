# MindArena Kubernetes Base

These manifests provide a local/dev Kubernetes deployment for the MindArena microservices backend.
They are intended for Minikube, Kind, Docker Desktop Kubernetes, or another local cluster.

## Images

Build the service images into your local cluster image store before applying the manifests.

For Minikube:

```bash
eval "$(minikube docker-env)"
docker build -t mindarena/discovery-server:0.1.0 services/discovery-server
docker build -t mindarena/config-server:0.1.0 services/config-server
docker build -t mindarena/api-gateway:0.1.0 services/api-gateway
docker build -t mindarena/identity-service:0.1.0 services/identity-service
docker build -t mindarena/challenge-service:0.1.0 services/challenge-service
docker build -t mindarena/ranking-service:0.1.0 services/ranking-service
docker build -t mindarena/notification-service:0.1.0 services/notification-service
docker build -t mindarena/submission-service:0.1.0 services/submission-service
docker build -t mindarena/chat-service:0.1.0 services/chat-service
```

## Deploy

Create the runtime secret from your secret manager before applying workloads:

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
```

```bash
kubectl apply -f k8s/base/config.yaml
kubectl apply -f k8s/base/security.yaml
kubectl apply -f k8s/base/infra.yaml
kubectl apply -f k8s/base/spring-cloud.yaml
kubectl apply -f k8s/base/domain-services.yaml
kubectl apply -f k8s/base/monitoring.yaml
```

Watch startup:

```bash
kubectl get pods -n mindarena -w
```

Open the gateway:

```bash
kubectl port-forward -n mindarena service/gateway 8082:8080
BASE_URL=http://localhost:8082 bash scripts/smoke-step4.sh
```

Open monitoring:

```bash
kubectl port-forward -n mindarena service/prometheus 9090:9090
kubectl port-forward -n mindarena service/grafana 3000:3000
```

## Notes

- The gateway is exposed as a `NodePort` on `30082` and can also be accessed by port-forwarding.
- MariaDB, Redis, and RabbitMQ use PVCs for local persistence.
- Secrets are not committed. `secrets.example.yaml` is a template only.
- The base includes restricted Pod Security labels, non-privileged container settings, network policies, resource quotas, disruption budgets, HPAs, and Prometheus/Grafana monitoring.
- Production still needs cluster-specific ingress/TLS, managed databases, external secret management, centralized logging, tracing, image signing policy, and backup/restore procedures.
