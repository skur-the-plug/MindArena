package com.mindarena.submission.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mindarena.submission.client.ChallengePolicyResponse;
import com.mindarena.submission.client.ChallengeServiceClient;
import com.mindarena.submission.client.IdentityServiceClient;
import com.mindarena.submission.client.UserVerificationResponse;
import com.mindarena.submission.dto.SubmissionRequest;
import com.mindarena.submission.model.Arena;
import com.mindarena.submission.model.Challenge;
import com.mindarena.submission.model.Submission;
import com.mindarena.submission.model.User;
import com.mindarena.submission.repository.ArenaRepository;
import com.mindarena.submission.repository.ChallengeRepository;
import com.mindarena.submission.repository.SubmissionCommentRepository;
import com.mindarena.submission.repository.SubmissionRepository;
import com.mindarena.submission.repository.UserRepository;
import com.mindarena.submission.repository.VoteRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class SubmissionApiServiceSecurityTest {

    @Mock private SubmissionRepository submissionRepository;
    @Mock private SubmissionCommentRepository commentRepository;
    @Mock private VoteRepository voteRepository;
    @Mock private ChallengeRepository challengeRepository;
    @Mock private UserRepository userRepository;
    @Mock private ArenaRepository arenaRepository;
    @Mock private IdentityServiceClient identityServiceClient;
    @Mock private ChallengeServiceClient challengeServiceClient;

    private SubmissionApiService service;
    private final SubmissionRequest request = new SubmissionRequest(9L, "answer", null);

    @BeforeEach
    void setUp() {
        service = new SubmissionApiService(
                submissionRepository,
                commentRepository,
                voteRepository,
                challengeRepository,
                userRepository,
                arenaRepository,
                identityServiceClient,
                challengeServiceClient
        );
    }

    @Test
    void identityRefusalLeavesNoPartialData() {
        when(identityServiceClient.verifyUser(7L))
                .thenThrow(new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "User refused"));

        assertThatThrownBy(() -> service.submit(request, 7L))
                .isInstanceOf(BusinessRuleException.class);

        verify(challengeServiceClient, never()).validateSubmission(any());
        verify(submissionRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void challengeRefusalLeavesNoPartialData() {
        when(identityServiceClient.verifyUser(7L))
                .thenReturn(new UserVerificationResponse(7L, "Secure User", "USER", true));
        when(challengeServiceClient.validateSubmission(9L))
                .thenThrow(new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Challenge closed"));

        assertThatThrownBy(() -> service.submit(request, 7L))
                .isInstanceOf(BusinessRuleException.class);

        verify(submissionRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        verify(arenaRepository, never()).save(any());
        verify(challengeRepository, never()).save(any());
    }

    @Test
    void authorizedFlowPersistsOnlyAfterBothChecks() {
        when(identityServiceClient.verifyUser(7L))
                .thenReturn(new UserVerificationResponse(7L, "Secure User", "USER", true));
        when(challengeServiceClient.validateSubmission(9L)).thenReturn(new ChallengePolicyResponse(
                9L,
                "Secure challenge",
                "Advanced",
                3L,
                "Coding Arena",
                LocalDateTime.now().plusDays(1),
                true
        ));
        when(userRepository.findById(7L)).thenReturn(Optional.empty());
        when(arenaRepository.findById(3L)).thenReturn(Optional.empty());
        when(challengeRepository.findById(9L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(arenaRepository.save(any(Arena.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(challengeRepository.save(any(Challenge.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Submission saved = service.submit(request, 7L);

        assertThat(saved.getAuthor().getId()).isEqualTo(7L);
        assertThat(saved.getChallenge().getId()).isEqualTo(9L);
        verify(identityServiceClient).verifyUser(7L);
        verify(challengeServiceClient).validateSubmission(9L);
        verify(submissionRepository).save(saved);
    }
}
