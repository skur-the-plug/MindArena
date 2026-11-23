package com.mindarena.identity.controller;

import com.mindarena.identity.dto.ServiceTokenResponse;
import com.mindarena.identity.security.ServiceClientRegistry;
import com.mindarena.identity.service.JwtService;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ServiceTokenController {

    private final ServiceClientRegistry serviceClientRegistry;
    private final JwtService jwtService;

    public ServiceTokenController(ServiceClientRegistry serviceClientRegistry, JwtService jwtService) {
        this.serviceClientRegistry = serviceClientRegistry;
        this.jwtService = jwtService;
    }

    @PostMapping("/internal/security/token")
    public ServiceTokenResponse token(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam String audience
    ) {
        String[] credentials = basicCredentials(authorization);
        var grant = serviceClientRegistry.authenticate(credentials[0], credentials[1], audience);
        return new ServiceTokenResponse(
                jwtService.generateServiceToken(grant.serviceId(), grant.audience(), grant.scopes()),
                "Bearer",
                jwtService.serviceExpirationSeconds()
        );
    }

    private String[] basicCredentials(String authorization) {
        if (authorization == null || !authorization.startsWith("Basic ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Service credentials are required");
        }
        try {
            String decoded = new String(
                    Base64.getDecoder().decode(authorization.substring("Basic ".length())),
                    StandardCharsets.UTF_8
            );
            int separator = decoded.indexOf(':');
            if (separator <= 0 || separator == decoded.length() - 1) {
                throw new IllegalArgumentException("Malformed credentials");
            }
            return new String[]{decoded.substring(0, separator), decoded.substring(separator + 1)};
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid service credentials");
        }
    }
}
