#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

(cd "$root_dir/services/identity-service" && mvn -q \
    -Dtest=JwtServiceTest,ServiceJwtDecoderTest,ServiceClientRegistryTest,ServiceTokenAuthorizationManagerTest test)
(cd "$root_dir/services/challenge-service" && mvn -q \
    -Dtest=ChallengeCatalogSecurityTest test)
(cd "$root_dir/services/submission-service" && mvn -q \
    -Dtest=SubmissionApiServiceSecurityTest,UserTokenValidatorTest test)

printf 'Service-security test matrix passed.\n'
