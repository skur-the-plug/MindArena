package com.mindarena.submission.client;

import java.time.LocalDateTime;

public record ChallengePolicyResponse(
        Long challengeId,
        String title,
        String difficulty,
        Long arenaId,
        String arenaName,
        LocalDateTime deadline,
        boolean accepted
) {
}
