package com.mindarena.notification.controller;

import com.mindarena.notification.dto.CreateNotificationRequest;
import com.mindarena.notification.dto.NotificationPayload;
import com.mindarena.notification.service.UserNotificationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/notifications")
public class NotificationApiController {

    private final UserNotificationService notificationService;

    public NotificationApiController(UserNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationPayload> notifications(@RequestHeader("X-User-Id") Long currentUserId) {
        return notificationService.notifications(currentUserId).stream().map(NotificationPayload::from).toList();
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@RequestHeader("X-User-Id") Long currentUserId) {
        return Map.of("unreadCount", notificationService.unreadCount(currentUserId));
    }

    @PostMapping
    public NotificationPayload create(@Valid @RequestBody CreateNotificationRequest request, @RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return NotificationPayload.from(notificationService.create(request));
    }

    @PostMapping("/{id}/read")
    public NotificationPayload markRead(@PathVariable Long id, @RequestHeader("X-User-Id") Long currentUserId) {
        return NotificationPayload.from(notificationService.markRead(currentUserId, id));
    }

    @DeleteMapping
    public void clear(@RequestHeader("X-User-Id") Long currentUserId) {
        notificationService.clear(currentUserId);
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
    }
}
