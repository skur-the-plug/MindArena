# Validation and Evidence Ledger

Validation date: **23 November 2026**<br>
Validated milestone: `secure-systems-gate-2` (`0e54055`)<br>
Environment: Java 21, Maven, Docker Compose, isolated Compose project
`mindarena-security-qa`

## Automated build evidence

| Check | Command | Result |
| --- | --- | --- |
| Security unit matrix | `bash scripts/test-service-security.sh` | PASS |
| All microservices | `bash scripts/build-microservices.sh` | PASS |
| Root application | `mvn -B verify` | PASS |
| Compose model | `docker compose ... config --quiet` | PASS |
| Patch hygiene | `git diff --check` | PASS |
| Shell syntax | `bash -n scripts/*.sh` for changed scripts | PASS |
| Source archive integrity | `unzip -tq MindArena-source-2026.zip` | PASS |
| Build from clean ZIP extraction | security matrix + all microservices + root `verify` | PASS |

The security unit matrix covers token claims and validation, exact authorization,
client-secret registry behavior, challenge policy, user-token validation, and
the no-partial-write orchestration rule.

## Runtime request matrix

The rows below are assertions in `scripts/smoke-service-security.sh`, executed
against a fresh stack and empty named volumes.

| Scenario | Target | Expected | Observed |
| --- | --- | ---: | ---: |
| No token | Identity internal user verification | 401 | 401 PASS |
| Malformed bearer token | Identity internal user verification | 401 | 401 PASS |
| User token on service-only endpoint | Identity internal user verification | 401 | 401 PASS |
| Spoofed `X-User-*` headers | Identity internal user verification | 401 | 401 PASS |
| Spoofed `X-User-*` headers, direct Submission call | Submission public API | 401 | 401 PASS |
| Service token on user endpoint | Submission public API | 401 | 401 PASS |
| Identity-audience token reused on Challenge | Challenge internal policy | 403 | 403 PASS |
| Correct Identity token | Identity internal user verification | 200 | 200 PASS |
| Correct Challenge token | Challenge internal policy | 200 | 200 PASS |
| Internal endpoint requested through Gateway | Gateway | 404 | 404 PASS |
| Identity refuses removed user | Full Submission flow | 422 | 422 PASS |
| Challenge refuses expired challenge | Full Submission flow | 422 | 422 PASS |
| Submission count after both refusals | MariaDB | unchanged | unchanged PASS |
| Valid user and active challenge | Full three-service flow | 200 | 200 PASS |

## Security properties demonstrated

- The same signature is insufficient without the required issuer, token type,
  caller, audience, scope, and expiry.
- Tokens are audience-bound: the Identity token cannot authorize Challenge.
- User and service tokens are not interchangeable.
- Caller-controlled identity headers cannot authorize the assessed flow.
- Submission persists only after Identity and Challenge both approve.
- Internal endpoints are absent from gateway routes.
- The same sanitized correlation ID is propagated through the three services.
- Raw bearer tokens and client credentials are not logged.

## Evidence still requiring human confirmation

These items cannot be truthfully automated by the repository:

- [x] Add Anas Houhou and Amr Azirar to the report cover.
- [x] Compile twice and visually inspect the 32-page ENSIAS PDF, contact sheet,
  cover and all figure-heavy pages.
- [ ] Replace the remaining teacher/encadrant `[À compléter]` field.
- [ ] Import the final ZIP into IntelliJ IDEA with Java 21 on the evaluator path.
- [ ] Optionally add two sanitized screenshots from the demo to the report.
- [ ] Upload the PDF and source ZIP to Moodle before 24 November 2026 midnight.
- [ ] Download or preview the submitted files and preserve Moodle confirmation.

## Presentation proof sequence

1. Show the policy table and private/public key placement.
2. Run the full smoke script and show the PASS line.
3. Point to the wrong-audience `403` assertion.
4. Point to the two `422` refusals and unchanged database row count.
5. Show one correlation ID across Submission, Identity, and Challenge logs.
6. Explain why short-lived JWTs were selected and what production hardening
   remains (TLS/mTLS, managed key rotation, revocation strategy, resilience).
