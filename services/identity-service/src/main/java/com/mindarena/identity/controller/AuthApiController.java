package com.mindarena.identity.controller;

import com.mindarena.identity.dto.AuthResponse;
import com.mindarena.identity.dto.LoginRequest;
import com.mindarena.identity.dto.RefreshTokenRequest;
import com.mindarena.identity.dto.RegisterRequest;
import com.mindarena.identity.dto.UserPayload;
import com.mindarena.identity.model.User;
import com.mindarena.identity.service.IdentityService;
import com.mindarena.identity.service.JwtService;
import com.mindarena.identity.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/identity/auth")
public class AuthApiController {

    private final IdentityService identityService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthApiController(
            IdentityService identityService,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.identityService = identityService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = identityService.register(request);
        return authResponse(user);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = identityService.authenticate(request.email(), request.password());
        return authResponse(user);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        User user = refreshTokenService.rotate(request.refreshToken());
        return authResponse(user);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    @GetMapping("/me")
    public UserPayload me(@RequestHeader("X-User-Id") Long currentUserId) {
        return UserPayload.from(identityService.requireUser(currentUserId));
    }

    private AuthResponse authResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(user),
                refreshTokenService.issue(user),
                "Bearer",
                jwtService.expirationSeconds(),
                UserPayload.from(user)
        );
    }
}
