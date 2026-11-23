package com.mindarena.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.mindarena.challenge.model.Challenge;
import com.mindarena.challenge.repository.ArenaRepository;
import com.mindarena.challenge.repository.ChallengeRepository;
import com.mindarena.challenge.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ChallengeCatalogSecurityTest {

    @Mock private ChallengeRepository challengeRepository;
    @Mock private ArenaRepository arenaRepository;
    @Mock private UserRepository userRepository;

    @Test
    void acceptsOnlyActiveChallengeBeforeDeadline() {
        ChallengeCatalogService service = service();
        Challenge open = challenge(true, LocalDateTime.now().plusHours(1));
        when(challengeRepository.findById(1L)).thenReturn(Optional.of(open));

        assertThat(service.requireOpenForSubmission(1L)).isSameAs(open);
    }

    @Test
    void rejectsInactiveAndExpiredChallenges() {
        ChallengeCatalogService service = service();
        when(challengeRepository.findById(1L))
                .thenReturn(Optional.of(challenge(false, LocalDateTime.now().plusHours(1))));
        when(challengeRepository.findById(2L))
                .thenReturn(Optional.of(challenge(true, LocalDateTime.now().minusSeconds(1))));

        assertThatThrownBy(() -> service.requireOpenForSubmission(1L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
        assertThatThrownBy(() -> service.requireOpenForSubmission(2L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    private ChallengeCatalogService service() {
        return new ChallengeCatalogService(challengeRepository, arenaRepository, userRepository);
    }

    private Challenge challenge(boolean active, LocalDateTime deadline) {
        Challenge challenge = new Challenge();
        challenge.setActive(active);
        challenge.setDeadline(deadline);
        return challenge;
    }
}
