package com.mindarena.challenge.controller;

import com.mindarena.challenge.dto.SubmissionPolicyPayload;
import com.mindarena.challenge.service.ChallengeCatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InternalChallengeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalChallengeController.class);

    private final ChallengeCatalogService challengeService;

    public InternalChallengeController(ChallengeCatalogService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping("/internal/challenges/{id}/submission-policy")
    public SubmissionPolicyPayload submissionPolicy(@PathVariable Long id, Authentication authentication) {
        LOGGER.info("Service {} is validating submission policy for challenge {}", authentication.getName(), id);
        return SubmissionPolicyPayload.from(challengeService.requireOpenForSubmission(id));
    }
}
