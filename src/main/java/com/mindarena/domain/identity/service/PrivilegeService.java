package com.mindarena.domain.identity.service;

import com.mindarena.domain.challenges.service.ChallengeJudgeService;
import com.mindarena.domain.challenges.model.Challenge;
import com.mindarena.domain.rankings.model.PlayerRank;
import com.mindarena.domain.identity.model.Role;
import com.mindarena.domain.identity.model.User;
import org.springframework.stereotype.Service;

@Service
public class PrivilegeService {

    private final ChallengeJudgeService challengeJudgeService;

    public PrivilegeService(ChallengeJudgeService challengeJudgeService) {
        this.challengeJudgeService = challengeJudgeService;
    }

    public boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }

    public boolean canVote(User user) {
        return isAdmin(user) || PlayerRank.fromScore(user.getScore()).getMinimumXp() >= PlayerRank.EXPLORER.getMinimumXp();
    }

    public boolean canCreateChallenge(User user) {
        return isAdmin(user) || PlayerRank.fromScore(user.getScore()).getMinimumXp() >= PlayerRank.CHALLENGER.getMinimumXp();
    }

    public boolean canManageChallenge(User user, Challenge challenge) {
        return isAdmin(user)
                || (canCreateChallenge(user)
                && challenge.getCreator() != null
                && challenge.getCreator().getId().equals(user.getId()));
    }

    public boolean canSelectBestAnswer(User user, Challenge challenge) {
        return canManageChallenge(user, challenge) || challengeJudgeService.isJudge(challenge, user);
    }

    public boolean appearsInActiveRankings(User user) {
        return isAdmin(user) || PlayerRank.fromScore(user.getScore()).getMinimumXp() >= PlayerRank.EXPLORER.getMinimumXp();
    }
}
