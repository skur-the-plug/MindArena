package com.mindarena.domain.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mindarena.domain.identity.model.User;
import com.mindarena.domain.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerEncodesPasswordBeforeSaving() {
        User submitted = new User();
        submitted.setFullName("Test User");
        submitted.setEmail("test@example.com");
        submitted.setPassword("plain-password");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.register(submitted);

        assertThat(saved.getPassword()).isEqualTo("encoded-password");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded-password");
    }

    @Test
    void registerRejectsDuplicateEmailWithoutEncodingOrSaving() {
        User submitted = new User();
        submitted.setEmail("taken@example.com");
        submitted.setPassword("plain-password");

        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(submitted))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}
