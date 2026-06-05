package com.mindarena.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mindarena.identity.dto.RegisterRequest;
import com.mindarena.identity.model.User;
import com.mindarena.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class IdentityServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private IdentityService identityService;

    @Test
    void registerEncodesPasswordAndPersistsProfileFields() {
        RegisterRequest request = new RegisterRequest(
                "Service User",
                "service@example.com",
                "plain-password",
                "java",
                "arenas"
        );
        when(userRepository.existsByEmail("service@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = identityService.register(request);

        assertThat(saved.getPassword()).isEqualTo("encoded-password");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("service@example.com");
        assertThat(captor.getValue().getSkills()).isEqualTo("java");
        assertThat(captor.getValue().getInterests()).isEqualTo("arenas");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "Service User",
                "service@example.com",
                "plain-password",
                "java",
                "arenas"
        );
        when(userRepository.existsByEmail("service@example.com")).thenReturn(true);

        assertThatThrownBy(() -> identityService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}
