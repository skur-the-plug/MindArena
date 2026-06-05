package com.mindarena.challenge.repository;

import com.mindarena.challenge.model.Arena;
import com.mindarena.challenge.model.Challenge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findByActiveTrueOrderByDeadlineAsc();

    List<Challenge> findByActiveFalseOrderByDeadlineDesc();

    List<Challenge> findByArenaAndActiveTrueOrderByDeadlineAsc(Arena arena);
}
