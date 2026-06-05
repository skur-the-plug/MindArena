package com.mindarena.notification.dto;

import com.mindarena.notification.model.UserNotification;
import java.time.LocalDateTime;

public record NotificationPayload(
        Long id,
        Long recipientId,
        String recipientName,
        String type,
        String message,
        String linkUrl,
        boolean read,
        LocalDateTime createdAt
) {
    public static NotificationPayload from(UserNotification notification) {
        return new NotificationPayload(
                notification.getId(),
                notification.getRecipient().getId(),
                notification.getRecipient().getFullName(),
                notification.getType().name(),
                notification.getMessage(),
                notification.getLinkUrl(),
                notification.isReadFlag(),
                notification.getCreatedAt()
        );
    }
}
