package com.mindarena.identity.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ServiceClientRegistry {

    private static final Map<String, Set<String>> SUBMISSION_GRANTS = Map.of(
            "mindarena-identity-service", Set.of("identity:user:verify"),
            "mindarena-challenge-service", Set.of("challenge:submission:validate")
    );

    private final String submissionClientId;
    private final byte[] submissionSecretHash;

    public ServiceClientRegistry(
            @Value("${mindarena.service-clients.submission.client-id}") String submissionClientId,
            @Value("${mindarena.service-clients.submission.secret-sha256}") String submissionSecretHash
    ) {
        this.submissionClientId = submissionClientId;
        try {
            this.submissionSecretHash = HexFormat.of().parseHex(submissionSecretHash);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Invalid submission service client secret hash", exception);
        }
        if (this.submissionSecretHash.length != 32) {
            throw new IllegalStateException("Submission service client secret hash must be SHA-256");
        }
    }

    public ServiceGrant authenticate(String clientId, String clientSecret, String audience) {
        if (!submissionClientId.equals(clientId)
                || !MessageDigest.isEqual(submissionSecretHash, sha256(clientSecret))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid service credentials");
        }
        Set<String> scopes = SUBMISSION_GRANTS.get(audience);
        if (scopes == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Audience is not allowed for this service");
        }
        return new ServiceGrant(submissionClientId, audience, scopes);
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record ServiceGrant(String serviceId, String audience, Set<String> scopes) {
    }
}
