package com.mindarena.identity.controller;

import com.mindarena.identity.dto.InternalUserVerification;
import com.mindarena.identity.service.IdentityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InternalUserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalUserController.class);

    private final IdentityService identityService;

    public InternalUserController(IdentityService identityService) {
        this.identityService = identityService;
    }

    @GetMapping("/internal/users/{id}/verify")
    public InternalUserVerification verify(@PathVariable Long id, Authentication authentication) {
        LOGGER.info("Service {} is verifying user {}", authentication.getName(), id);
        return InternalUserVerification.from(identityService.requireUser(id));
    }
}
