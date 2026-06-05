package com.mindarena.challenge.dto;

import java.time.LocalDateTime;

public record ChallengeRequest(
        Long arenaId,
        Long creatorId,
        String title,
        String brief,
        String difficulty,
        String templateType,
        String submissionTemplateName,
        String submissionTemplateBody,
        LocalDateTime deadline
) {
}
