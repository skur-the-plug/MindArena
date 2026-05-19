package com.mindarena.event;

public record XpAwardedEvent(
        Long userId,
        int points,
        int score,
        String reason,
        String linkUrl
) {
}
