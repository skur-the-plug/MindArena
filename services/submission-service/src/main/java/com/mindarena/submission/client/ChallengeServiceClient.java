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
public class ChallengeServiceClient {

    public static final String AUDIENCE = "mindarena-challenge-service";

    private final RestClient client;
    private final ServiceTokenProvider tokenProvider;

    public ChallengeServiceClient(
            @LoadBalanced RestClient.Builder builder,
            ServiceTokenProvider tokenProvider,
            @Value("${mindarena.service-client.challenge-base-url}") String baseUrl
    ) {
        this.client = builder.clone().baseUrl(baseUrl).build();
        this.tokenProvider = tokenProvider;
    }

    public ChallengePolicyResponse validateSubmission(Long challengeId) {
        try {
            ChallengePolicyResponse response = client.get()
                    .uri("/internal/challenges/{id}/submission-policy", challengeId)
                    .headers(headers -> headers.setBearerAuth(tokenProvider.tokenFor(AUDIENCE)))
                    .retrieve()
                    .body(ChallengePolicyResponse.class);
            if (response == null || !response.accepted()) {
                throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Challenge does not accept submissions");
            }
            return response;
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new BusinessRuleException(HttpStatus.NOT_FOUND, "Challenge not found");
            }
            if (exception.getStatusCode().value() == 422) {
                throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Challenge does not accept submissions");
            }
            if (exception.getStatusCode().value() == 401 || exception.getStatusCode().value() == 403) {
                throw new DownstreamSecurityException("Challenge rejected the Submission service token");
            }
            throw new DownstreamUnavailableException("Challenge policy validation failed", exception);
        } catch (ResourceAccessException | RestClientResponseException exception) {
            throw new DownstreamUnavailableException("Challenge policy validation is unavailable", exception);
        }
    }
}
