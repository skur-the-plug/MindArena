package com.mindarena.domain.identity.repository;

import com.mindarena.domain.identity.model.Role;
import com.mindarena.domain.identity.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findTop10ByRoleOrderByScoreDesc(Role role);

    List<User> findByRoleOrderByScoreDesc(Role role, Pageable pageable);
}
