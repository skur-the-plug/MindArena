package com.mindarena.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.mindarena.identity.model.RefreshToken;
import com.mindarena.identity.model.User;
import com.mindarena.identity.repository.RefreshTokenRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void issueStoresOnlyTokenHash() {
        RefreshTokenService refreshTokenService = new RefreshTokenService(refreshTokenRepository, 30);
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        User user = new User();
        user.setEmail("service@example.com");

        String token = refreshTokenService.issue(user);

        RefreshToken saved = captor.getValue();
        assertThat(token).hasSize(96);
        assertThat(saved.getTokenHash()).hasSize(64);
        assertThat(saved.getTokenHash()).isNotEqualTo(token);
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now().plusDays(29));
    }

    @Test
    void rotateReturnsUserAndRevokesPreviousToken() {
        RefreshTokenService refreshTokenService = new RefreshTokenService(refreshTokenRepository, 30);
        User user = new User();
        user.setEmail("service@example.com");
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User refreshedUser = refreshTokenService.rotate("refresh-token");

        assertThat(refreshedUser).isSameAs(user);
        assertThat(refreshToken.isRevoked()).isTrue();
        assertThatThrownBy(() -> refreshTokenService.rotate("refresh-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid refresh token");
    }
}
