package com.mindarena.submission.client;

import com.mindarena.submission.service.BusinessRuleException;
import com.mindarena.submission.service.DownstreamSecurityException;
import com.mindarena.submission.service.DownstreamUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class IdentityServiceClient {

    public static final String AUDIENCE = "mindarena-identity-service";

    private final RestClient client;
    private final ServiceTokenProvider tokenProvider;

    public IdentityServiceClient(
            @LoadBalanced RestClient.Builder builder,
            ServiceTokenProvider tokenProvider,
            @Value("${mindarena.service-client.identity-base-url}") String baseUrl
    ) {
        this.client = builder.clone().baseUrl(baseUrl).build();
        this.tokenProvider = tokenProvider;
    }

    public UserVerificationResponse verifyUser(Long userId) {
        try {
            UserVerificationResponse response = client.get()
                    .uri("/internal/users/{id}/verify", userId)
                    .headers(headers -> headers.setBearerAuth(tokenProvider.tokenFor(AUDIENCE)))
                    .retrieve()
                    .body(UserVerificationResponse.class);
            if (response == null || !response.eligible()) {
                throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "User is not eligible to submit");
            }
            return response;
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "User does not exist");
            }
            if (exception.getStatusCode().value() == 401 || exception.getStatusCode().value() == 403) {
                throw new DownstreamSecurityException("Identity rejected the Submission service token");
            }
            throw new DownstreamUnavailableException("Identity user verification failed", exception);
        } catch (ResourceAccessException | RestClientResponseException exception) {
            throw new DownstreamUnavailableException("Identity user verification is unavailable", exception);
        }
    }
}
