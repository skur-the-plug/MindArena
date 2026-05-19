package com.mindarena.domain.rankings.dto;

public record LeaderboardPayload(
        long userId,
        String name,
        String avatarUrl,
        long xp,
        String rank
) {

    public static LeaderboardPayload from(LeaderboardEntry entry) {
        return new LeaderboardPayload(
                entry.getUser().getId(),
                entry.getUser().getFullName(),
                entry.getUser().getAvatarUrl(),
                entry.getXp(),
                entry.getRank().getLabel()
        );
    }
}
