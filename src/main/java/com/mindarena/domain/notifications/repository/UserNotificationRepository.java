package com.mindarena.domain.notifications.repository;

import com.mindarena.domain.identity.model.User;
import com.mindarena.domain.notifications.model.UserNotification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findTop30ByRecipientOrderByCreatedAtDesc(User recipient);

    long countByRecipientAndReadFlagFalse(User recipient);

    void deleteByRecipient(User recipient);

    Optional<UserNotification> findByIdAndRecipient(Long id, User recipient);
}
