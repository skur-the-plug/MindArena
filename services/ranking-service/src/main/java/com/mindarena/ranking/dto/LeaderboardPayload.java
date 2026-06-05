package com.mindarena.ranking.dto;

public record LeaderboardPayload(
        Long userId,
        String fullName,
        long xp,
        String rank
) {
    public static LeaderboardPayload from(LeaderboardEntry entry) {
        return new LeaderboardPayload(
                entry.getUser().getId(),
                entry.getUser().getFullName(),
                entry.getXp(),
                entry.getRank().getLabel()
        );
    }
}
