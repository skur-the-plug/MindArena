package com.mindarena.repository;

import com.mindarena.model.Arena;
import com.mindarena.model.Challenge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findByActiveTrueOrderByDeadlineAsc();

    List<Challenge> findByActiveFalseOrderByDeadlineDesc();

    List<Challenge> findByArenaAndActiveTrueOrderByDeadlineAsc(Arena arena);
}
