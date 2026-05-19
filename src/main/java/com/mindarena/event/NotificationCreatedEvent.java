package com.mindarena.event;

public record NotificationCreatedEvent(
        Long notificationId,
        Long recipientId,
        String type,
        String message,
        String linkUrl
) {
}
