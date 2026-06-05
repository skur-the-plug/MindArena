package com.mindarena.ranking.repository;

import com.mindarena.ranking.model.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
}
