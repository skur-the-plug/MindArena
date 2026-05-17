package com.mindarena.repository;

import com.mindarena.model.Challenge;
import com.mindarena.model.ChallengeJudge;
import com.mindarena.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeJudgeRepository extends JpaRepository<ChallengeJudge, Long> {
    boolean existsByChallengeAndJudge(Challenge challenge, User judge);

    List<ChallengeJudge> findByChallenge(Challenge challenge);

    void deleteByChallengeAndJudge(Challenge challenge, User judge);
}
