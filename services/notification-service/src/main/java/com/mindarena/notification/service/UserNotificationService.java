package com.mindarena.notification.service;

import com.mindarena.notification.dto.CreateNotificationRequest;
import com.mindarena.notification.model.NotificationType;
import com.mindarena.notification.model.User;
import com.mindarena.notification.model.UserNotification;
import com.mindarena.notification.repository.UserNotificationRepository;
import com.mindarena.notification.repository.UserRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserNotificationService {

    private final UserRepository userRepository;
    private final UserNotificationRepository notificationRepository;

    public UserNotificationService(UserRepository userRepository, UserNotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    public List<UserNotification> notifications(Long userId) {
        if (userId == null) {
            return notificationRepository.findByOrderByCreatedAtDesc(PageRequest.of(0, 30));
        }
        return notificationRepository.findTop30ByRecipientOrderByCreatedAtDesc(requireUser(userId));
    }

    public long unreadCount(Long userId) {
        return notificationRepository.countByRecipientAndReadFlagFalse(requireUser(userId));
    }

    @Transactional
    public UserNotification create(CreateNotificationRequest request) {
        UserNotification notification = new UserNotification();
        notification.setRecipient(requireUser(request.recipientId()));
        notification.setType(NotificationType.valueOf(request.type()));
        notification.setMessage(request.message());
        notification.setLinkUrl(request.linkUrl());
        return notificationRepository.save(notification);
    }

    @Transactional
    public UserNotification markRead(Long userId, Long notificationId) {
        User user = requireUser(userId);
        UserNotification notification = notificationRepository.findByIdAndRecipient(notificationId, user)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setReadFlag(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void clear(Long userId) {
        notificationRepository.deleteByRecipient(requireUser(userId));
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
