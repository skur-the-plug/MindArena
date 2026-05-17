package com.mindarena.repository;

import com.mindarena.model.Arena;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArenaRepository extends JpaRepository<Arena, Long> {
    Optional<Arena> findByName(String name);
}
