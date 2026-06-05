package com.mindarena.notification.controller;

import com.mindarena.notification.dto.CreateNotificationRequest;
import com.mindarena.notification.dto.NotificationPayload;
import com.mindarena.notification.service.UserNotificationService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationApiController {

    private final UserNotificationService notificationService;

    public NotificationApiController(UserNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationPayload> notifications(@RequestParam(required = false) Long userId) {
        return notificationService.notifications(userId).stream().map(NotificationPayload::from).toList();
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@RequestParam Long userId) {
        return Map.of("unreadCount", notificationService.unreadCount(userId));
    }

    @PostMapping
    public NotificationPayload create(@RequestBody CreateNotificationRequest request) {
        return NotificationPayload.from(notificationService.create(request));
    }

    @PostMapping("/{id}/read")
    public NotificationPayload markRead(@PathVariable Long id, @RequestParam Long userId) {
        return NotificationPayload.from(notificationService.markRead(userId, id));
    }

    @DeleteMapping
    public void clear(@RequestParam Long userId) {
        notificationService.clear(userId);
    }
}
