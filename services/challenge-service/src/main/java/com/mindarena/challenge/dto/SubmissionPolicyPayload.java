package com.mindarena.challenge.dto;

import com.mindarena.challenge.model.Challenge;
import java.time.LocalDateTime;

public record SubmissionPolicyPayload(
        Long challengeId,
        String title,
        String difficulty,
        Long arenaId,
        String arenaName,
        LocalDateTime deadline,
        boolean accepted
) {
    public static SubmissionPolicyPayload from(Challenge challenge) {
        return new SubmissionPolicyPayload(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDifficulty(),
                challenge.getArena().getId(),
                challenge.getArena().getName(),
                challenge.getDeadline(),
                true
        );
    }
}
