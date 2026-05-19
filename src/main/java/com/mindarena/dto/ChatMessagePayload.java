package com.mindarena.dto;

import com.mindarena.model.ChatMessage;
import java.time.format.DateTimeFormatter;

public record ChatMessagePayload(
        Long id,
        String authorName,
        String authorAvatarUrl,
        String content,
        String createdAt,
        String displayTime
) {

    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("dd MMM HH:mm");

    public static ChatMessagePayload from(ChatMessage message) {
        return new ChatMessagePayload(
                message.getId(),
                message.getAuthor().getFullName(),
                message.getAuthor().getAvatarUrl(),
                message.getContent(),
                message.getCreatedAt().toString(),
                message.getCreatedAt().format(DISPLAY_TIME)
        );
    }
}
