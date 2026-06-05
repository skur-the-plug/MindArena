package com.mindarena.notification.dto;

public record CreateNotificationRequest(
        Long recipientId,
        String type,
        String message,
        String linkUrl
) {
}
