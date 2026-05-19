package com.mindarena.domain.challenges.repository;

import com.mindarena.domain.challenges.model.Challenge;
import com.mindarena.domain.challenges.model.ChallengeJudge;
import com.mindarena.domain.identity.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeJudgeRepository extends JpaRepository<ChallengeJudge, Long> {
    boolean existsByChallengeAndJudge(Challenge challenge, User judge);

    List<ChallengeJudge> findByChallenge(Challenge challenge);

    void deleteByChallengeAndJudge(Challenge challenge, User judge);
}
