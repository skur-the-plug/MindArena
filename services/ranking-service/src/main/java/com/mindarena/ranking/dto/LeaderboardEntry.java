package com.mindarena.ranking.dto;

import com.mindarena.ranking.model.PlayerRank;
import com.mindarena.ranking.model.User;
import java.io.Serializable;

public class LeaderboardEntry implements Serializable {

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
