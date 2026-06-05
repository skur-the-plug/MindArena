package com.mindarena.notification.repository;

import com.mindarena.notification.model.User;
import com.mindarena.notification.model.UserNotification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findTop30ByRecipientOrderByCreatedAtDesc(User recipient);

    List<UserNotification> findByOrderByCreatedAtDesc(Pageable pageable);

    long countByRecipientAndReadFlagFalse(User recipient);

    void deleteByRecipient(User recipient);

    Optional<UserNotification> findByIdAndRecipient(Long id, User recipient);
}
