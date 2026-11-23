# MindArena Secure Systems — Partner and Agent Handoff

## Read this first

This file is the source of truth for anyone joining the project, including a
coding agent. Work from the repository state, preserve the security boundaries
below, and do not replace the focused academic contribution with a generic
platform rewrite.

Assignment deadline: **24 November 2026 before midnight**. The assignment is a
two-person project.

## Assignment requirements summarized

The supplied assignment asks for a Java solution that:

1. analyzes service-to-service authentication mechanisms;
2. selects and implements at least one authentication mechanism;
3. selects and implements an authorization model;
4. demonstrates collaboration among at least three microservices;
5. explains the choices, alternatives, implementation, and security evidence;
6. delivers a report and an IntelliJ-importable source archive.

Our selected solution is:

- Authentication: short-lived RS256 service JWTs.
- Authorization: registered service identity plus least-privilege scopes.
- Three services: Identity, Challenge, and Submission.
- Business request: creating a submission only after Identity verifies the user
  and Challenge validates the submission policy.

## Project status

| Stage | Status | Evidence |
| --- | --- | --- |
| Clean pre-project baseline | Complete | Commit/tag `mindarena-clean-baseline` |
| Three-service security implementation | Complete | Automated service-security tests and fresh Docker runtime smoke test |
| Academic report and final delivery package | Ready pending administrative field and Moodle upload | ENSIAS LaTeX report, evidence ledger, evaluator guide, presentation guide and verified archive tooling added |

The normal application still uses gateway-centered user authentication for
several legacy flows. Do not claim that every MindArena service call is secured.
The implemented academic scope is the Submission write flow described here.

## Exact request flow

```text
User JWT
   |
   v
API Gateway
   |
   v
Submission Service -- service JWT [aud=identity, scope=identity:user:verify]
   |                                                           |
   |                                                           v
   |                                                    Identity Service
   |
   +---------------- service JWT [aud=challenge,
                                  scope=challenge:submission:validate]
                                                               |
                                                               v
                                                        Challenge Service

Both checks succeed -> update verified projections -> persist submission
Either check refuses/fails -> persist nothing -> return coherent API error
```

The gateway validates the external token, but Submission also validates the
original user JWT and derives the user ID from its signed subject. Spoofed
`X-User-*` headers therefore do not authorize the secure flow.

## Security contract

Service tokens contain and enforce:

- `iss=mindarena-identity-service`
- `sub=mindarena-submission-service`
- exact target `aud`
- exact operation `scope`
- `token_use=service`
- short `iat`/`exp` lifetime (120 seconds by default)

Identity is the only service with the RSA private key. Gateway, Challenge, and
Submission receive only the public key. Submission authenticates to the token
endpoint with an externalized high-entropy secret; Identity stores only its
SHA-256 digest. Tokens are cached separately per audience until shortly before
expiry and are never logged.

Policy table:

| Caller | Target | Required scope |
| --- | --- | --- |
| Submission | Identity | `identity:user:verify` |
| Submission | Challenge | `challenge:submission:validate` |

Every unlisted caller, audience, endpoint, or scope is denied.

## Important files

| Purpose | Location |
| --- | --- |
| Full design and threat model | `docs/security/service-to-service-security.md` |
| Identity token issuer/registry | `services/identity-service/.../security/` and `ServiceTokenController` |
| Identity internal user check | `InternalUserController` |
| Challenge resource server/check | `services/challenge-service/.../security/` and `InternalChallengeController` |
| Submission service clients/cache | `services/submission-service/.../client/` |
| Atomic orchestration | `SubmissionApiService` |
| User-JWT defense in depth | `services/submission-service/.../security/` |
| Narrow network rule | `k8s/base/security.yaml` |
| Automated security tests | `scripts/test-service-security.sh` |
| Runtime attack/demo script | `scripts/smoke-service-security.sh` |
| Loopback-only test exposure | `docker-compose.security-test.yml` |

## Local setup

```bash
cp .env.example .env
# Fill the non-generated database, RabbitMQ, and Grafana placeholders.
bash scripts/generate-jwt-keys.sh --write-env .env
bash scripts/generate-service-credentials.sh --write-env .env
docker compose up -d --build
```

Never commit `.env`, raw private keys, or raw client secrets.

## Verification commands

Fast automated security matrix:

```bash
bash scripts/test-service-security.sh
```

All service tests:

```bash
bash scripts/build-microservices.sh
mvn -B verify
```

Full runtime proof, including attacks and no-partial-data checks:

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

The override exposes Identity, Challenge, and Submission only on loopback ports
for negative security probes. Normal Compose does not publish those ports.

## Expected security evidence

- Correct service token completes the three-service request.
- Missing, malformed, expired, forged, wrong-issuer, and user tokens fail
  service-token authentication.
- Wrong audience, caller, or scope fails authorization.
- A service token cannot be used as a Submission user token.
- A direct `X-User-Id` spoof against Submission fails.
- Identity refusal and Challenge refusal independently leave no submission row.
- Internal endpoints are absent from gateway routes.
- One sanitized correlation ID appears across all three services.

## Remaining work — delivery checklist

Do not redesign the implementation unless a failing test proves it necessary.
The next stage is evidence and presentation:

1. Replace `[À compléter]` on the report cover with the teacher/encadrant name.
2. Import the generated source archive in IntelliJ with Java 21 as a human check.
3. Rehearse the demo and likely questions: why JWT instead of mTLS, why RS256,
   rotation/revocation, replay, Identity outage, audience vs scope, and default deny.
   A timed script and answers are ready in `docs/report/PRESENTATION_GUIDE.md`.
4. Upload the PDF and source archive to Moodle, reopen them, and retain confirmation.

## Collaboration rules

- Pull `main`, then use a narrow branch such as `docs/report` or
  `test/security-evidence`; avoid both partners editing the same file.
- Keep commits small and descriptive. Do not commit generated credentials.
- Do not expose `/internal/**` through the gateway.
- Do not weaken audience, caller, scope, issuer, expiry, or `token_use` checks to
  make a demo pass.
- Do not log bearer tokens, Basic credentials, or secret hashes.
- Update this handoff if architecture or remaining work changes.

Suggested coding-agent prompt:

> Read `README.md`, `docs/security/PROJECT_HANDOFF.md`, and
> `docs/security/service-to-service-security.md` completely. Inspect the current
> Git status and tests. Work only on the requested remaining delivery item,
> preserve the documented security contract, never expose or commit secrets, run
> the relevant verification commands, and report exact evidence and files changed.
