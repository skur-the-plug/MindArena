package com.mindarena.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mindarena.domain.challenges.repository.ArenaRepository;
import com.mindarena.domain.challenges.repository.ArenaMembershipRepository;
import com.mindarena.domain.challenges.repository.ChallengeRepository;
import com.mindarena.domain.challenges.repository.PlatformNewsRepository;
import com.mindarena.domain.identity.model.Role;
import com.mindarena.domain.identity.model.User;
import com.mindarena.domain.identity.repository.UserRepository;
import com.mindarena.domain.submissions.repository.SubmissionCommentRepository;
import com.mindarena.domain.submissions.repository.SubmissionRepository;
import com.mindarena.domain.submissions.repository.VoteRepository;
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
    private ArenaMembershipRepository arenaMembershipRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private PlatformNewsRepository platformNewsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private SubmissionCommentRepository submissionCommentRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void runDoesNotCreateDemoAdminWhenDisabled() {
        DataInitializer initializer = initializer(false, "ignored", false, "");
        when(userRepository.findByEmail("admin@mindarena.local")).thenReturn(Optional.empty());
        when(challengeRepository.findAll()).thenReturn(List.of());
        when(platformNewsRepository.count()).thenReturn(1L);

        initializer.run();

        verify(userRepository, never()).existsByEmail("admin@mindarena.local");
    }

    @Test
    void runCreatesDemoAdminWithConfiguredPasswordWhenEnabled() {
        DataInitializer initializer = initializer(true, "external-password", false, "");
        when(userRepository.existsByEmail("admin@mindarena.local")).thenReturn(false);
        when(userRepository.findByEmail("admin@mindarena.local")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("external-password")).thenReturn("encoded-external-password");
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

    @Test
    void runDoesNotCreateDemoUsersWhenDemoDataIsDisabled() {
        DataInitializer initializer = initializer(false, "", false, "");
        when(userRepository.findByEmail("admin@mindarena.local")).thenReturn(Optional.empty());
        when(challengeRepository.findAll()).thenReturn(List.of());
        when(platformNewsRepository.count()).thenReturn(1L);

        initializer.run();

        verify(passwordEncoder, never()).encode("local-demo-password");
    }

    @Test
    void runRequiresExternalPasswordWhenDemoDataIsEnabled() {
        DataInitializer initializer = initializer(false, "", true, "");
        when(userRepository.findByEmail("admin@mindarena.local")).thenReturn(Optional.empty());
        when(challengeRepository.findAll()).thenReturn(List.of());
        when(platformNewsRepository.count()).thenReturn(1L);

        org.assertj.core.api.Assertions.assertThatThrownBy(initializer::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("demo-data.user-password");
    }

    private DataInitializer initializer(
            boolean demoAdminEnabled,
            String demoAdminPassword,
            boolean demoDataEnabled,
            String demoUserPassword
    ) {
        return new DataInitializer(
                arenaRepository,
                arenaMembershipRepository,
                challengeRepository,
                platformNewsRepository,
                userRepository,
                submissionRepository,
                submissionCommentRepository,
                voteRepository,
                passwordEncoder,
                demoAdminEnabled,
                "admin@mindarena.local",
                demoAdminPassword,
                demoDataEnabled,
                demoUserPassword
        );
    }
}
