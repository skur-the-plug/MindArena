package com.mindarena.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mindarena.domain.challenges.repository.ArenaRepository;
import com.mindarena.domain.challenges.repository.ChallengeRepository;
import com.mindarena.domain.challenges.repository.PlatformNewsRepository;
import com.mindarena.domain.identity.model.Role;
import com.mindarena.domain.identity.model.User;
import com.mindarena.domain.identity.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private ArenaRepository arenaRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private PlatformNewsRepository platformNewsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void runDoesNotCreateDemoAdminWhenDisabled() {
        DataInitializer initializer = initializer(false, "ignored");
        when(challengeRepository.count()).thenReturn(0L);
        when(challengeRepository.findAll()).thenReturn(List.of());
        when(platformNewsRepository.count()).thenReturn(1L);

        initializer.run();

        verify(userRepository, never()).existsByEmail("admin@mindarena.local");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void runCreatesDemoAdminWithConfiguredPasswordWhenEnabled() {
        DataInitializer initializer = initializer(true, "external-password");
        when(userRepository.existsByEmail("admin@mindarena.local")).thenReturn(false);
        when(userRepository.findByEmail("admin@mindarena.local")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("external-password")).thenReturn("encoded-external-password");
        when(challengeRepository.count()).thenReturn(0L);
        when(challengeRepository.findAll()).thenReturn(List.of());
        when(platformNewsRepository.count()).thenReturn(1L);

        initializer.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("admin@mindarena.local");
        assertThat(saved.getPassword()).isEqualTo("encoded-external-password");
        assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
    }

    private DataInitializer initializer(boolean demoAdminEnabled, String demoAdminPassword) {
        return new DataInitializer(
                arenaRepository,
                challengeRepository,
                platformNewsRepository,
                userRepository,
                passwordEncoder,
                demoAdminEnabled,
                "admin@mindarena.local",
                demoAdminPassword
        );
    }
}
