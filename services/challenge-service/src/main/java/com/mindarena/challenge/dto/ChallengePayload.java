package com.mindarena.challenge.dto;

import com.mindarena.challenge.model.Challenge;
import java.time.LocalDateTime;

public record ChallengePayload(
        Long id,
        String title,
        String brief,
        Long arenaId,
        String arenaName,
        Long creatorId,
        String creatorName,
        String difficulty,
        String templateType,
        String submissionTemplateName,
        LocalDateTime deadline,
        boolean active
) {
    public static ChallengePayload from(Challenge challenge) {
        return new ChallengePayload(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getBrief(),
                challenge.getArena().getId(),
                challenge.getArena().getName(),
                challenge.getCreator() == null ? null : challenge.getCreator().getId(),
                challenge.getCreator() == null ? null : challenge.getCreator().getFullName(),
                challenge.getDifficulty(),
                challenge.getTemplateType() == null ? null : challenge.getTemplateType().name(),
                challenge.getSubmissionTemplateName(),
                challenge.getDeadline(),
                challenge.isActive()
        );
    }
}
