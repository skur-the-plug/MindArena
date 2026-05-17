package com.mindarena.dto;

import com.mindarena.model.PlayerRank;
import com.mindarena.model.User;

public class LeaderboardEntry {

    private final User user;
    private final long xp;

    public LeaderboardEntry(User user, long xp) {
        this.user = user;
        this.xp = xp;
    }

    public User getUser() {
        return user;
    }

    public long getXp() {
        return xp;
    }

    public PlayerRank getRank() {
        return PlayerRank.fromScore((int) xp);
    }
}
