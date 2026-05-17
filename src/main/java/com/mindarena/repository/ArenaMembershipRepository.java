package com.mindarena.repository;

import com.mindarena.model.Arena;
import com.mindarena.model.ArenaMembership;
import com.mindarena.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArenaMembershipRepository extends JpaRepository<ArenaMembership, Long> {
    boolean existsByUserAndArena(User user, Arena arena);

    List<ArenaMembership> findByUser(User user);

    List<ArenaMembership> findByArena(Arena arena);
}
