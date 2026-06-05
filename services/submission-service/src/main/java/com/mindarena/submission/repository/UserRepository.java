package com.mindarena.submission.repository;

import com.mindarena.submission.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
