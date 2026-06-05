package com.mindarena.identity.service;

import com.mindarena.identity.dto.ProfileUpdateRequest;
import com.mindarena.identity.dto.RegisterRequest;
import com.mindarena.identity.model.User;
import com.mindarena.identity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdentityService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public IdentityService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setSkills(request.skills());
        user.setInterests(request.interests());
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(Long id, ProfileUpdateRequest request) {
        User user = requireUser(id);
        user.setFullName(request.fullName());
        user.setSkills(request.skills());
        user.setInterests(request.interests());
        user.setProfileImageUrl(request.profileImageUrl());
        return userRepository.save(user);
    }
}
