package com.mindarena.identity.controller;

import com.mindarena.identity.dto.ProfileUpdateRequest;
import com.mindarena.identity.dto.RegisterRequest;
import com.mindarena.identity.dto.UserPayload;
import com.mindarena.identity.repository.UserRepository;
import com.mindarena.identity.service.IdentityService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/identity")
public class IdentityApiController {

    private final IdentityService identityService;
    private final UserRepository userRepository;

    public IdentityApiController(IdentityService identityService, UserRepository userRepository) {
        this.identityService = identityService;
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<UserPayload> users(@RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return userRepository.findAll(PageRequest.of(0, 50)).stream()
                .map(UserPayload::from)
                .toList();
    }

    @GetMapping("/users/{id}")
    public UserPayload user(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-User-Role") String role
    ) {
        requireSelfOrAdmin(id, currentUserId, role);
        return UserPayload.from(identityService.requireUser(id));
    }

    @PostMapping("/users")
    public UserPayload register(@Valid @RequestBody RegisterRequest request, @RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return UserPayload.from(identityService.register(request));
    }

    @PutMapping("/users/{id}/profile")
    public UserPayload updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody ProfileUpdateRequest request,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-User-Role") String role
    ) {
        requireSelfOrAdmin(id, currentUserId, role);
        return UserPayload.from(identityService.updateProfile(id, request));
    }

    private void requireSelfOrAdmin(Long requestedUserId, Long currentUserId, String role) {
        if (!requestedUserId.equals(currentUserId) && !"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
    }
}
