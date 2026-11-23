package com.mindarena.submission.client;

import com.mindarena.submission.service.DownstreamSecurityException;
import com.mindarena.submission.service.DownstreamUnavailableException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class ServiceTokenProvider {

    private final RestClient identityClient;
    private final String clientId;
    private final String clientSecret;
    private final Map<String, CachedToken> cache = new ConcurrentHashMap<>();

    public ServiceTokenProvider(
            @LoadBalanced RestClient.Builder builder,
            @Value("${mindarena.service-client.identity-base-url}") String identityBaseUrl,
            @Value("${mindarena.service-client.client-id}") String clientId,
            @Value("${mindarena.service-client.client-secret}") String clientSecret
    ) {
        this.identityClient = builder.clone().baseUrl(identityBaseUrl).build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String tokenFor(String audience) {
        CachedToken current = cache.get(audience);
        if (current != null && Instant.now().isBefore(current.renewAfter())) {
            return current.value();
        }
        synchronized (cache) {
            current = cache.get(audience);
            if (current != null && Instant.now().isBefore(current.renewAfter())) {
                return current.value();
            }
            ServiceTokenResponse response = requestToken(audience);
            long cacheSeconds = Math.max(1, response.expiresInSeconds() - 15);
            cache.put(audience, new CachedToken(response.accessToken(), Instant.now().plusSeconds(cacheSeconds)));
            return response.accessToken();
        }
    }

    private ServiceTokenResponse requestToken(String audience) {
        try {
            ServiceTokenResponse response = identityClient.post()
                    .uri(uri -> uri.path("/internal/security/token")
                            .queryParam("audience", audience)
                            .build())
                    .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                    .retrieve()
                    .body(ServiceTokenResponse.class);
            if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
                throw new DownstreamUnavailableException("Identity returned an empty service token");
            }
            return response;
        } catch (HttpClientErrorException exception) {
            HttpStatusCode status = exception.getStatusCode();
            if (status.value() == 401 || status.value() == 403) {
                throw new DownstreamSecurityException("Service credentials were rejected by Identity");
            }
            throw new DownstreamUnavailableException("Identity could not issue a service token", exception);
        } catch (ResourceAccessException | RestClientResponseException exception) {
            throw new DownstreamUnavailableException("Identity service token endpoint is unavailable", exception);
        }
    }

    private record CachedToken(String value, Instant renewAfter) {
    }
}
