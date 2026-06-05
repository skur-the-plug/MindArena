package com.mindarena.domain.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.mindarena.domain.challenges.model.Challenge;
import com.mindarena.domain.challenges.service.ChallengeJudgeService;
import com.mindarena.domain.identity.model.Role;
import com.mindarena.domain.identity.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PrivilegeServiceTest {

    @Mock
    private ChallengeJudgeService challengeJudgeService;

    @Test
    void adminCanVoteCreateAndAppearInRankings() {
        PrivilegeService privilegeService = new PrivilegeService(challengeJudgeService);
        User admin = user(1L, Role.ADMIN, 0);

        assertThat(privilegeService.canVote(admin)).isTrue();
        assertThat(privilegeService.canCreateChallenge(admin)).isTrue();
        assertThat(privilegeService.appearsInActiveRankings(admin)).isTrue();
    }

    @Test
    void rookieCannotVoteOrCreateChallenges() {
        PrivilegeService privilegeService = new PrivilegeService(challengeJudgeService);
        User rookie = user(2L, Role.USER, 99);

        assertThat(privilegeService.canVote(rookie)).isFalse();
        assertThat(privilegeService.canCreateChallenge(rookie)).isFalse();
        assertThat(privilegeService.appearsInActiveRankings(rookie)).isFalse();
    }

    @Test
    void explorerCanVoteButCannotCreateChallenges() {
        PrivilegeService privilegeService = new PrivilegeService(challengeJudgeService);
        User explorer = user(3L, Role.USER, 100);

        assertThat(privilegeService.canVote(explorer)).isTrue();
        assertThat(privilegeService.canCreateChallenge(explorer)).isFalse();
    }

    @Test
    void challengeCreatorWithEnoughScoreCanManageOwnChallenge() {
        PrivilegeService privilegeService = new PrivilegeService(challengeJudgeService);
        User creator = user(4L, Role.USER, 300);
        Challenge challenge = new Challenge();
        challenge.setCreator(creator);

        assertThat(privilegeService.canManageChallenge(creator, challenge)).isTrue();
    }

    @Test
    void assignedJudgeCanSelectBestAnswerWithoutManagingChallenge() {
        PrivilegeService privilegeService = new PrivilegeService(challengeJudgeService);
        User creator = user(5L, Role.USER, 300);
        User judge = user(6L, Role.USER, 100);
        Challenge challenge = new Challenge();
        challenge.setCreator(creator);
        when(challengeJudgeService.isJudge(challenge, judge)).thenReturn(true);

        assertThat(privilegeService.canManageChallenge(judge, challenge)).isFalse();
        assertThat(privilegeService.canSelectBestAnswer(judge, challenge)).isTrue();
    }

    private User user(Long id, Role role, int score) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        user.setFullName("User " + id);
        user.setEmail("user" + id + "@example.com");
        user.setPassword("encoded-password");
        user.setRole(role);
        user.addScore(score);
        return user;
    }
}
