package com.mindarena.domain.challenges.repository;

import com.mindarena.domain.challenges.model.Arena;
import com.mindarena.domain.challenges.model.Challenge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findByActiveTrueOrderByDeadlineAsc();

    List<Challenge> findByActiveFalseOrderByDeadlineDesc();

    List<Challenge> findByArenaAndActiveTrueOrderByDeadlineAsc(Arena arena);
}
