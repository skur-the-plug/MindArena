package com.mindarena.service;

import com.mindarena.model.NotificationType;
import com.mindarena.model.PlatformNews;
import com.mindarena.model.PlayerRank;
import com.mindarena.model.User;
import com.mindarena.model.UserNotification;
import com.mindarena.repository.PlatformNewsRepository;
import com.mindarena.repository.UserNotificationRepository;
import com.mindarena.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final UserNotificationRepository notificationRepository;
    private final PlatformNewsRepository platformNewsRepository;
    private final UserRepository userRepository;

    public NotificationService(
            UserNotificationRepository notificationRepository,
            PlatformNewsRepository platformNewsRepository,
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.platformNewsRepository = platformNewsRepository;
        this.userRepository = userRepository;
    }

    public List<UserNotification> personalNotifications(User user) {
        return notificationRepository.findTop30ByRecipientOrderByCreatedAtDesc(user);
    }

    public long unreadCount(User user) {
        return notificationRepository.countByRecipientAndReadFlagFalse(user);
    }

    public List<PlatformNews> platformNews() {
        return platformNewsRepository.findTop20ByOrderByCreatedAtDesc();
    }

    public void notify(User recipient, NotificationType type, String message) {
        notify(recipient, type, message, null);
    }

    public void notify(User recipient, NotificationType type, String message, String linkUrl) {
        if (recipient != null) {
            notificationRepository.save(new UserNotification(recipient, type, message, linkUrl));
        }
    }

    @Transactional
    public void awardXp(User user, int points, String reason) {
        awardXp(user, points, reason, null);
    }

    @Transactional
    public void awardXp(User user, int points, String reason, String linkUrl) {
        PlayerRank before = PlayerRank.fromScore(user.getScore());
        user.addScore(points);
        userRepository.save(user);
        notify(user, NotificationType.XP, reason + " +" + points + " XP earned.", linkUrl);
        PlayerRank after = PlayerRank.fromScore(user.getScore());
        if (after != before) {
            notify(user, NotificationType.RANK, "Your rank changed from " + before.getLabel() + " to " + after.getLabel() + ".", "/profile");
        }
    }

    @Transactional
    public String markReadAndResolveLink(User user, Long notificationId) {
        UserNotification notification = notificationRepository.findByIdAndRecipient(notificationId, user)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setReadFlag(true);
        notificationRepository.save(notification);
        if (notification.getLinkUrl() == null || notification.getLinkUrl().isBlank()) {
            return "/news";
        }
        return notification.getLinkUrl();
    }

    public PlatformNews publishNews(User author, String title, String body, String category) {
        PlatformNews news = new PlatformNews();
        news.setAuthor(author);
        news.setTitle(title);
        news.setBody(body);
        news.setCategory(category);
        return platformNewsRepository.save(news);
    }

    @Transactional
    public void clearNotifications(User user) {
        notificationRepository.deleteByRecipient(user);
    }

    @Transactional
    public void clearPlatformNews() {
        platformNewsRepository.deleteAll();
    }
}
