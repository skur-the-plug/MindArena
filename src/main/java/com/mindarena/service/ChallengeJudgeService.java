package com.mindarena.service;

import com.mindarena.model.Challenge;
import com.mindarena.model.ChallengeJudge;
import com.mindarena.model.NotificationType;
import com.mindarena.model.PlayerRank;
import com.mindarena.model.Role;
import com.mindarena.model.User;
import com.mindarena.repository.ChallengeJudgeRepository;
import com.mindarena.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChallengeJudgeService {

    private final ChallengeJudgeRepository challengeJudgeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ChallengeJudgeService(ChallengeJudgeRepository challengeJudgeRepository, UserRepository userRepository, NotificationService notificationService) {
        this.challengeJudgeRepository = challengeJudgeRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public List<ChallengeJudge> judgesFor(Challenge challenge) {
        return challengeJudgeRepository.findByChallenge(challenge);
    }

    public boolean isJudge(Challenge challenge, User user) {
        return challengeJudgeRepository.existsByChallengeAndJudge(challenge, user);
    }

    public List<User> eligibleJudges() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.USER)
                .filter(user -> PlayerRank.fromScore(user.getScore()).getMinimumXp() >= PlayerRank.EXPLORER.getMinimumXp())
                .toList();
    }

    public List<User> eligibleJudgesFor(Challenge challenge) {
        Long creatorId = challenge.getCreator() == null ? null : challenge.getCreator().getId();
        return eligibleJudges().stream()
                .filter(user -> creatorId == null || !user.getId().equals(creatorId))
                .filter(user -> !challengeJudgeRepository.existsByChallengeAndJudge(challenge, user))
                .toList();
    }

    public void addJudge(Challenge challenge, User judge) {
        if (!challengeJudgeRepository.existsByChallengeAndJudge(challenge, judge)) {
            challengeJudgeRepository.save(new ChallengeJudge(challenge, judge));
            notificationService.notify(judge, NotificationType.JUDGE, "You were assigned as a judge for: " + challenge.getTitle() + ".", "/challenges/" + challenge.getId());
        }
    }

    @Transactional
    public void removeJudge(Challenge challenge, User judge) {
        challengeJudgeRepository.deleteByChallengeAndJudge(challenge, judge);
    }
}
