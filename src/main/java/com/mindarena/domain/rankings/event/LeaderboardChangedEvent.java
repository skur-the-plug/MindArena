package com.mindarena.domain.rankings.event;

public record LeaderboardChangedEvent(Long arenaId, Long challengeId) {

    public static LeaderboardChangedEvent globalOnly() {
        return new LeaderboardChangedEvent(null, null);
    }
}
