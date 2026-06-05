package com.mindarena.chat.dto;

import com.mindarena.chat.model.ChatMessage;
import java.time.LocalDateTime;

public record ChatMessagePayload(Long id, String roomType, Long authorId, String authorName, Long arenaId,
                                 Long challengeId, String content, LocalDateTime createdAt) {
    public static ChatMessagePayload from(ChatMessage message) {
        return new ChatMessagePayload(
                message.getId(),
                message.getRoomType().name(),
                message.getAuthor().getId(),
                message.getAuthor().getFullName(),
                message.getArena() == null ? null : message.getArena().getId(),
                message.getChallenge() == null ? null : message.getChallenge().getId(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
