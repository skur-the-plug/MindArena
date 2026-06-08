package com.mindarena.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(
        @NotNull
        Long recipientId,
        @NotBlank @Pattern(regexp = "XP|RANK|CHALLENGE|SUBMISSION|SYSTEM")
        String type,
        @NotBlank @Size(max = 600)
        String message,
        @Size(max = 500)
        String linkUrl
) {
}
