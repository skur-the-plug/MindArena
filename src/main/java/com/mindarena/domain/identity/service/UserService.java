package com.mindarena.domain.identity.service;

import com.mindarena.domain.identity.model.User;
import com.mindarena.domain.identity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateProfile(User existing, User submitted) {
        existing.setFullName(submitted.getFullName());
        existing.setSkills(submitted.getSkills());
        existing.setInterests(submitted.getInterests());
        return userRepository.save(existing);
    }

    public User updateProfileImage(User user, String profileImageUrl) {
        user.setProfileImageUrl(profileImageUrl);
        return userRepository.save(user);
    }
}
