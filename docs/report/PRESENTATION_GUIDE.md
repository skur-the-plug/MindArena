# MindArena — Presentation and Defense Guide

## Five-minute deterministic demo

### 0:00–0:40 — Problem

“MindArena originally authenticated users at the gateway, but an internal
service call did not carry its own cryptographic identity and permission. Our
goal was to secure one real request involving Submission, Identity, and
Challenge according to Zero Trust.”

Show the architecture/policy page of the report.

### 0:40–1:30 — Design

- Identity issues RS256 service JWTs valid for 120 seconds.
- Only Identity has the private key; consumers have the public key.
- The subject identifies Submission.
- Each target has a separate audience and narrow scope.
- Every unlisted combination is denied.

Show these two policy rows:

| Target | Audience | Scope |
| --- | --- | --- |
| Identity | `mindarena-identity-service` | `identity:user:verify` |
| Challenge | `mindarena-challenge-service` | `challenge:submission:validate` |

### 1:30–2:30 — Request flow

1. Gateway validates the external user token.
2. Submission validates it again and derives the signed user ID.
3. Submission calls Identity with an Identity-audience service token.
4. Submission calls Challenge with a Challenge-audience service token.
5. Only after both approvals does the transaction persist the submission.

Point out that `X-User-*` headers are not an authority.

### 2:30–3:45 — Live proof

Run the already-started isolated stack, then:

```bash
COMPOSE_PROJECT_NAME=mindarena-security-qa \
  bash scripts/smoke-service-security.sh
```

Explain that the single PASS result contains assertions for:

- missing/malformed/user tokens → 401;
- wrong audience → 403;
- internal route through the gateway → 404;
- Identity and Challenge refusals → 422 and unchanged row count;
- valid three-service request → 200.

### 3:45–4:30 — Defense in depth

- Internal endpoints are absent from gateway routes.
- Kubernetes permits only Submission → Identity/Challenge for this flow.
- Correlation IDs cross all three services; secrets and tokens do not enter logs.
- User tokens and service tokens have different validation contracts.

### 4:30–5:00 — Trade-offs

“JWT was selected for visible application claims and local validation. mTLS is
complementary transport authentication. In production we would add TLS/mTLS,
managed JWKS rotation, a secret manager, stronger resilience, and potentially a
full OAuth 2.0 authorization server.”

## Suggested binôme split

| Member | Primary responsibility | Must also understand |
| --- | --- | --- |
| Member 1 | Problem, comparisons, architecture, JWT claims | Test matrix and limits |
| Member 2 | Implementation flow, demo, evidence, threat model | Decision criteria and policy |

Both members should be able to explain the two policy rows and the absence of
partial writes.

## Likely questions and concise answers

### Why JWT instead of mTLS?

JWT directly expresses the target audience and operation-level permission, and
receivers validate locally. mTLS authenticates the connection and encrypts it,
but does not alone express `identity:user:verify`. A production design can use
both.

### Why RS256 instead of HS256?

With HS256, every validator holding the shared secret could mint tokens. RS256
keeps signing authority in Identity; Submission and Challenge receive only the
public verification key.

### Why not a complete OAuth 2.0 server?

The implementation follows the client-credentials pattern but keeps the
academic contribution small and inspectable. A standards-complete server is the
preferred production evolution for discovery, administration, and rotation.

### How is replay handled?

Tokens expire after 120 seconds, are target-audience-bound and narrowly scoped.
This limits but does not eliminate the replay window. TLS protects transport; a
`jti` deny list is possible when immediate revocation justifies its state and
operational cost.

### How are tokens revoked?

Normal revocation is bounded by the short expiration. For an incident, rotate
the client secret or signing key. Production can use key identifiers, overlapping
JWKS rotation, introspection, or a deny list.

### What happens if Identity is down?

Already issued tokens remain locally verifiable until expiry. New token issuance
fails closed; Submission returns a coherent 503 and persists nothing.

### Audience versus scope?

Audience states which service may accept the token. Scope states which operation
the caller may perform there. Both must match; neither replaces the other.

### Why keep `token_use`?

It prevents a valid user JWT from being confused with a service credential. The
services enforce distinct user and service validation contracts.

### Why store SHA-256 of the client secret?

Identity does not need the raw secret after enrollment. It hashes the presented
secret and compares digests in constant time. The raw secret exists only in
Submission's runtime secret store.

### Is the whole MindArena platform now Zero Trust?

No. The assessed and verified scope is the Submission write flow. Claiming more
would be misleading. The same pattern can be rolled out incrementally to other
service calls.

## Backup if the live stack fails

Use, in order:

1. `docs/report/VALIDATION_EVIDENCE.md` for the exact result table.
2. The report's validation page for presentation.
3. `scripts/smoke-service-security.sh` to show the executable assertions.
4. The Git tag `secure-systems-gate-2` for the validated implementation state.
