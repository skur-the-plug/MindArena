package com.mindarena.domain.notifications.service;

import com.mindarena.domain.rankings.event.LeaderboardChangedEvent;
import com.mindarena.domain.notifications.event.NotificationCreatedEvent;
import com.mindarena.domain.notifications.event.XpAwardedEvent;
import com.mindarena.domain.notifications.model.NotificationType;
import com.mindarena.domain.challenges.model.PlatformNews;
import com.mindarena.domain.rankings.model.PlayerRank;
import com.mindarena.domain.identity.model.User;
import com.mindarena.domain.notifications.model.UserNotification;
import com.mindarena.domain.challenges.repository.PlatformNewsRepository;
import com.mindarena.domain.notifications.repository.UserNotificationRepository;
import com.mindarena.domain.identity.repository.UserRepository;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final UserNotificationRepository notificationRepository;
    private final PlatformNewsRepository platformNewsRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationService(
            UserNotificationRepository notificationRepository,
            PlatformNewsRepository platformNewsRepository,
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.notificationRepository = notificationRepository;
        this.platformNewsRepository = platformNewsRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
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
            UserNotification notification = notificationRepository.save(new UserNotification(recipient, type, message, linkUrl));
            eventPublisher.publishEvent(new NotificationCreatedEvent(
                    notification.getId(),
                    recipient.getId(),
                    type.name(),
                    message,
                    linkUrl
            ));
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
        eventPublisher.publishEvent(new XpAwardedEvent(
                user.getId(),
                points,
                user.getScore(),
                reason,
                linkUrl
        ));
        eventPublisher.publishEvent(LeaderboardChangedEvent.globalOnly());
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
