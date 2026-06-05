package com.mindarena.ranking.repository;

import com.mindarena.ranking.model.Role;
import com.mindarena.ranking.model.User;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRoleOrderByScoreDesc(Role role, Pageable pageable);
}
