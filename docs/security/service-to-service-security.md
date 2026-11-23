# Service-to-Service Security Design

## Fixed scenario

The academic contribution secures one real write path:

1. A user sends `POST /api/submissions` with a user access token.
2. The gateway validates that user token, replaces caller-supplied identity headers,
   and forwards the original token to Submission Service.
3. Submission independently validates the user JWT and derives the user ID from its
   signed subject; caller-supplied user headers are not an authority for this flow.
4. Submission Service obtains a short-lived service token for Identity Service and
   verifies that the user exists and is eligible.
5. Submission Service obtains a separately scoped token for Challenge Service and
   verifies that the challenge exists, is active, and has not passed its deadline.
6. Only after both checks succeed does Submission Service update its local
   projections and persist the submission in one database transaction.

The three collaborating domain services are Identity, Challenge, and Submission.
The gateway is the external entry point, not one of the three academic services.

## Selected mechanism and authorization model

Authentication uses RS256 service JWTs issued by Identity Service. Only Identity
holds the private signing key. Receiving services validate the public key, issuer,
expiry, `token_use=service`, caller (`sub`), and target audience (`aud`). Tokens
expire after 120 seconds by default and are cached by Submission Service until 15
seconds before expiry.

Authorization uses service RBAC plus scopes. A registered service identity is the
role; scopes grant one narrow operation. Everything not listed below is denied.

| Caller | Target | Audience | Required scope | Operation |
| --- | --- | --- | --- | --- |
| `mindarena-submission-service` | Identity | `mindarena-identity-service` | `identity:user:verify` | Verify one user for submission |
| `mindarena-submission-service` | Challenge | `mindarena-challenge-service` | `challenge:submission:validate` | Validate one challenge's submission policy |

Submission authenticates to the token endpoint with an externalized, high-entropy
client secret. Identity stores only its SHA-256 digest and compares it in constant
time. The raw secret exists only in Submission's runtime secret store. Use
`scripts/generate-service-credentials.sh --write-env .env` for local rotation.

## Trust boundaries

- `/internal/**` is not routed by the gateway.
- NetworkPolicy allows Submission to reach only the Identity and Challenge domain
  services required by this flow.
- Authorization is derived only from verified JWTs. `X-User-*` headers have no
  authority on internal endpoints or on the secured Submission API.
- User JWTs cannot be confused with service JWTs because they do not carry the
  required issuer, `token_use`, audience, subject, and scope combination.
- Each downstream audience receives its own token; a token intended for Identity
  cannot be reused against Challenge.

## Threat model

| Threat | Control | Remaining consideration |
| --- | --- | --- |
| Service spoofing | Registered client ID, high-entropy secret, constant-time digest comparison | Rotate the secret after suspected exposure |
| Token theft | 120-second expiry, audience restriction, narrow scope, no token logging | TLS is required outside local development |
| Replay | Short lifetime and audience binding limit the replay window | Add `jti` deny-listing only if the operational cost is justified |
| Privilege escalation | Default deny, exact caller/audience/scope checks | Review the policy table when adding a service |
| Direct internal access | JWT resource servers plus Kubernetes ingress policy | Host-level access remains an infrastructure concern |
| User/service token confusion | Required issuer and `token_use=service`; caller and audience authorization | Keep separate signing keys as a future hardening option |
| Header spoofing | Submission derives the user from the signed JWT; internal endpoints ignore user headers | Do not expose domain service ports publicly |
| Signing-key exposure | Private key is injected only into Identity | Use a secret manager/HSM and rotation in production |
| Downstream outage | Coherent 503 response and no persistence before both checks | Circuit breakers are a possible production extension |

## Error semantics and observability

- `401`: missing, malformed, expired, forged, wrong-issuer, or non-service token.
- `403`: valid service token with the wrong caller, audience, or scope.
- `404`/`422`: user or challenge business validation refused the request.
- `502`: an internal service rejected Submission's configured service authority.
- `503`: token issuance or a downstream validation service is unavailable.

The gateway creates or sanitizes `X-Correlation-Id`; all three services return it,
propagate it on internal calls, and place it in structured log context. Tokens and
client secrets are never written to logs.

## Why this mechanism

RS256 JWTs make the academic properties visible in code and tests while allowing
receivers to validate locally when Identity is temporarily unavailable. Compared
with mTLS, this implementation demonstrates application-level audiences and
permissions more directly and has a lighter local certificate lifecycle. Compared
with a full OAuth2/OIDC authorization server, it keeps the project focused while
retaining the client-credentials pattern. mTLS remains complementary transport
hardening, not a replacement for operation-level authorization.
