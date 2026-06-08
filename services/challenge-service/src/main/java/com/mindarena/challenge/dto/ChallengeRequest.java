package com.mindarena.challenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record ChallengeRequest(
        @NotNull
        Long arenaId,
        @NotBlank @Size(max = 120)
        String title,
        @NotBlank @Size(max = 1200)
        String brief,
        @Size(max = 30)
        String difficulty,
        @Pattern(regexp = "CODE_SOLUTION|SYSTEM_DESIGN_FIX|STARTUP_PITCH_CANVAS|GROWTH_STRATEGY_PLAN|CREATIVE_CONCEPT_BRIEF|VISUAL_STORYBOARD|ARGUMENT_BATTLE|CRITICAL_ANALYSIS_CASE")
        String templateType,
        @Size(max = 80)
        String submissionTemplateName,
        @Size(max = 4000)
        String submissionTemplateBody,
        LocalDateTime deadline
) {
}
