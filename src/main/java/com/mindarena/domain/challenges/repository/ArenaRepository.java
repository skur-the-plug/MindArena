package com.mindarena.domain.challenges.repository;

import com.mindarena.domain.challenges.model.Arena;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArenaRepository extends JpaRepository<Arena, Long> {
    Optional<Arena> findByName(String name);
}
