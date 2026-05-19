package com.mindarena.domain.challenges.repository;

import com.mindarena.domain.challenges.model.Arena;
import com.mindarena.domain.challenges.model.ArenaMembership;
import com.mindarena.domain.identity.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArenaMembershipRepository extends JpaRepository<ArenaMembership, Long> {
    boolean existsByUserAndArena(User user, Arena arena);

    List<ArenaMembership> findByUser(User user);

    List<ArenaMembership> findByArena(Arena arena);
}
