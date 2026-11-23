package com.mindarena.identity.security;

import java.util.Collection;
import java.util.function.Supplier;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

public class ServiceTokenAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final String audience;
    private final String caller;
    private final String scope;

    public ServiceTokenAuthorizationManager(String audience, String caller, String scope) {
        this.audience = audience;
        this.caller = caller;
        this.scope = scope;
    }

    @Override
    public AuthorizationDecision check(
            Supplier<Authentication> authenticationSupplier,
            RequestAuthorizationContext context
    ) {
        Authentication authentication = authenticationSupplier.get();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication) || !authentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }
        Collection<String> audiences = jwtAuthentication.getToken().getAudience();
        boolean allowed = "service".equals(jwtAuthentication.getToken().getClaimAsString("token_use"))
                && caller.equals(jwtAuthentication.getName())
                && audiences.contains(audience)
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("SCOPE_" + scope));
        return new AuthorizationDecision(allowed);
    }
}
