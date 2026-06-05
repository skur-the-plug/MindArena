package com.mindarena.chat.dto;

public record PostMessageRequest(String roomType, Long authorId, Long arenaId, Long challengeId, String content) {
}
