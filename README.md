# SSM (Security Session Manager)

SSM is a Spring Boot microservice that manages card security operations — generating and
validating **PIN**, **OTP**, and **CVV2** — for the banking platform. It is a thin
orchestration layer: session lifecycle, locking, retry policy, rate limiting, and audit
logging are implemented **locally** in this service, while the actual cryptographic
generation/verification of PIN, OTP and CVV2 values is **delegated over REST to the
external Vault/Security service** (`vault-service` / `security`), which owns the key
material and crypto operations.

```
Client → SSM (session, locking, retry policy, rate limiting, audit, persistence)
              │
              └── REST ──▶ Vault/Security service (actual PIN/OTP/CVV crypto)
```

## What SSM owns vs. what it delegates

| Owned locally by SSM | Delegated to the Vault/Security service |
|---|---|
| Session creation/validation/invalidation | Generating PIN, OTP, CVV2 values |
| Card lock / unblock state (`LockState`) | Verifying PIN, OTP, CVV2 values |
| Retry policy & permanent lock on abuse | Encryption/hashing of the secret values |
| Rate limiting (generate / view secret) | |
| Audit log of security events | |
| Storage of PIN1 (encrypted+hash) and CVV2 metadata | |
| Status reference data | |

CVV2 and OTP are never persisted by SSM (PCI / one-time-use); only PIN1 is stored,
encrypted with its hash, in `card_secret`.

## Modules

Multi-module Maven project, hexagonal (ports & adapters) architecture:

```
ssm-service (root)/
├── ssm-container/            Spring Boot entry point, config (DB, Redis, Eureka, Flyway,
│                              ShedLock, OpenAPI, Security)
├── ssm-application/          REST controllers (input adapters)
├── ssm-domain/
│   ├── ssm-domain-core/      Domain model, enums, exceptions (no framework dependencies)
│   └── ssm-application-service/  Use cases: services, handlers, ports (input/output),
│                              mappers, commands/responses
├── ssm-infrastructure/       Output adapters: JPA repositories, the Vault REST client,
│                              search/utility helpers
└── ssm-messaging/            Reserved for future async/messaging integration (currently empty)
```

## REST API

Base path: `/api/v1/ssm`

| Method | Path | Purpose |
|---|---|---|
| POST | `/sessions` | Create a new SSM session for card operations |
| POST | `/sessions/{sessionId}/invalidate` | Invalidate a session |
| POST | `/card/generate` | Generate PIN1, OTP, or CVV2 (`X-Session-Id` header) |
| POST | `/card/validate` | Validate a previously generated PIN/OTP/CVV2 |
| GET | `/card/secrets/view` | View the stored PIN1 (rate-limited; CVV2 can never be viewed) |
| POST | `/card/unblock` | Release an active lock on a card |
| POST/PUT/GET | `/status/**` | Reference status data (create/update/list) |
| GET | `/enums/**` | Enum metadata for clients |

Full request/response contracts and examples are documented via OpenAPI/Swagger UI
(see `OpenApiConfig`).

## Configuration highlights (`application*.yml`)

- **Port**: `8009` (SSL enabled)
- **Database**: Oracle (`ssm` schema), migrated with Flyway
- **Cache/locks**: Redis, ShedLock (distributed scheduler locks)
- **Service discovery**: Eureka
- **Vault/Security service**: `vault.base.url` (per-environment; e.g. `application.yml`,
  `application-development.yml`, `application-staging.yml`)
- **Rate limiting**: configurable per operation under `ssm.rate-limit.*`

Scheduled jobs (`DeleteExpiredOtpsJob`, `DeleteExpiredSessionsJob`) run nightly to purge
expired OTPs and sessions.

All configuration is externalized via environment variables with local-dev defaults
(`localhost`) baked in — nothing in the repo points at internal `192.168.x.x` addresses
or ships a real credential. See `docker-compose.yml` for the full list of variables and
`k8s/base/secret.yaml.example` for what a deployment needs to supply.

## Build & run

Requires JDK 21.

```bash
mvn -DskipTests package
java -jar ssm-container/target/ssm-container-*.jar
```

### Local stack (Docker Compose)

`docker-compose.yml` brings up everything SSM needs to run end-to-end locally: Oracle
(`gvenzl/oracle-free`), Redis, an OpenTelemetry Collector, and Elasticsearch + Kibana for
logs/traces. Eureka registration is disabled locally (`EUREKA_CLIENT_ENABLED=false`) since
no Eureka server is bundled.

```bash
docker compose up --build
# App:        https://localhost:8009  (self-signed dev cert)
# Swagger UI: https://localhost:8009/swagger-ui.html
# Kibana:     http://localhost:5601
```

## Observability

- **Tracing & metrics**: Micrometer + OpenTelemetry, exported via OTLP
  (`management.otlp.tracing.endpoint`, `management.otlp.metrics.export.url`) to the OTel
  Collector, which forwards to Elasticsearch (`observability/otel-collector-config.yaml`).
- **Logs**: structured JSON on stdout (`logstash-logback-encoder`) in non-dev profiles,
  plain-text console in dev; both also ship directly to the OTel Collector via
  `OpenTelemetryAppender` (`logback-spring.xml`), correlated with trace/span IDs.
- **Health probes**: `/actuator/health/liveness` and `/actuator/health/readiness` (split
  probes enabled) for use as Kubernetes liveness/readiness checks. `/actuator/prometheus`
  is exposed for metrics scraping.
- Actuator exposure is restricted to `health,info,metrics,prometheus` in every profile —
  it used to be `include: "*"` (env, shutdown, heapdump, etc. all public), which was
  fixed as part of this pass.

## Resilience

Calls from SSM to the Vault/Security service (`VaultRepositoryImpl`) are wrapped with
Resilience4j `@CircuitBreaker` + `@Retry` (`resilience4j.*` in `application.yml`,
instance name `vaultService`) so a slow or failing Vault service degrades gracefully
(fails fast via `SsmDomainException`) instead of hanging every request or cascading.

## Testing

JUnit 5 + Mockito + AssertJ unit tests cover the highest-risk logic: the request
validators, `CardServiceImpl` orchestration (session/lock/retry paths), `CvvServiceImpl`,
`CardDataMapper`, and a regression test for the `LockStateRepositoryImpl.release()` fix
below. Run with:

```bash
mvn test
```

This is a starting baseline, not full coverage — the infrastructure/JPA layer and the
REST controllers don't have tests yet (would need `@DataJpaTest`/Testcontainers for the
former and `@WebMvcTest`/MockMvc for the latter).

## Kubernetes

`k8s/base/` has a Kustomize base: `Namespace`, `ConfigMap`, `Deployment` (rolling update,
resource requests/limits, non-root, startup/liveness/readiness probes on the actuator
health groups above), `Service`, `PodDisruptionBudget`, and an `HorizontalPodAutoscaler`.
Secrets are **not** included — copy `k8s/base/secret.yaml.example` to `secret.yaml` (it's
gitignored), fill in real values or wire up a proper secret manager, and apply it
separately from the rest:

```bash
kubectl apply -k k8s/base
kubectl apply -f k8s/base/secret.yaml   # after copying from the .example template
```

The `ConfigMap` points at in-cluster DNS names for Oracle/Redis/Eureka/Vault/OTel as
placeholders — update them (or override via Kustomize overlays) for your actual cluster
topology.

### Service mesh / mTLS (Istio)

`k8s/base` has no Istio dependency and applies on any cluster. `k8s/components/istio-mtls`
is an optional Kustomize [Component](https://kubectl.docs.kubernetes.io/guides/config_management/components/)
that turns on mutual TLS for SSM's traffic on clusters that have Istio installed:

- `PeerAuthentication` (`STRICT`) — SSM only accepts mesh-authenticated (mTLS) traffic.
- `DestinationRule` (`ISTIO_MUTUAL`) — outbound calls to the Vault/Security service
  originate mTLS using SSM's workload certificate.
- `AuthorizationPolicy` — only mesh-authenticated callers reach SSM at all; tighten
  `source.principals` once the calling services' exact identities are known.

No application code changes needed — the sidecar proxy (enabled via the
`istio-injection: enabled` label on the `ssm` namespace) transparently upgrades
plain-HTTP traffic to mTLS on the wire; `VaultRepositoryImpl` keeps calling
`vault.base.url` over plain HTTP as before.

```bash
kubectl apply -k k8s/overlays/istio-mesh   # base + Istio mTLS, on an Istio-enabled cluster
kubectl apply -k k8s/base                  # base only, everywhere else
```

**This only secures the SSM side.** End-to-end mTLS requires the Vault/Security service
(a separate repository, out of scope here) to also be mesh-enrolled with a matching
`PeerAuthentication`. Until then, treat this as one-sided and keep `STRICT` mode in mind —
it will reject the Vault service's calls back if it isn't in the mesh yet; switch to
`PERMISSIVE` as an interim step if needed.

## Authentication & authorization

Token issuance and role/claim assignment for this platform are **not** handled inside
SSM — a separate internal service owns that responsibility end-to-end. An identity
provider (Keycloak) is also part of the organization's infrastructure, but is
intentionally **not documented in this repository for security reasons**. See "Known
issues" #2 below for what that means for SSM's current state, and the mTLS section above
for how service-to-service calls are secured independently of that token layer.

## Engineering notes

### Hardening applied

- **Credential hygiene**: removed hardcoded database/cache passwords and CI registry
  credentials from tracked files; everything now resolves via environment variables
  with safe local-dev defaults (`docker-compose.yml`, `k8s/base/secret.yaml.example`).
- **Dependency injection correctness**: `VaultRepositoryImpl` (real client) and
  `MockVaultRepository` (test double) are cleanly separated by Spring profile, so the
  correct implementation is always the one wired at runtime.
- **Resilience**: `VaultRepositoryImpl` calls are wrapped with Resilience4j
  circuit-breaker + retry so a degraded downstream dependency fails fast instead of
  cascading.
- **Correctness fix**: `LockStateRepositoryImpl.release()` — reordered to resolve the
  lock before deleting it, with a regression test covering the fix.
- **API surface**: Actuator exposure narrowed to `health,info,metrics,prometheus`;
  Swagger/OpenAPI security scheme names aligned across controllers.
- **Toolchain consistency**: all modules build against Java 21.

### Deliberate scope boundaries

- **Authentication/authorization** is intentionally out of this service — see
  "Authentication & authorization" above. `SecurityConfig` is a placeholder pending a
  platform-level decision on whether SSM validates tokens itself, trusts an upstream
  gateway, or both.
- **`ValidateRequestValidator`** has a known gap in how OTP/PIN validate requests map
  to validation rules; current behavior is captured by a test rather than silently
  changed, pending a product decision on the intended contract.
