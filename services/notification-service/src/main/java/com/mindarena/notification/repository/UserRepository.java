package com.mindarena.notification.repository;

import com.mindarena.notification.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
