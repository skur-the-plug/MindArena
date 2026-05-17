package com.mindarena.model;

import java.util.List;

public enum PlayerRank {
    ROOKIE("Rookie", 0, "View arenas, join arenas, view challenges, and submit answers."),
    EXPLORER("Explorer", 100, "All Rookie privileges, plus voting on submissions and active leaderboard ranking."),
    CHALLENGER("Challenger", 300, "All Explorer privileges, plus creating challenges and managing your own challenges."),
    EXPERT("Expert", 700, "Challenger privileges with higher-status arena presence."),
    CHAMPION("Champion", 1500, "Challenger privileges at the top public rank.");

    private final String label;
    private final int minimumXp;
    private final String privilege;

    PlayerRank(String label, int minimumXp, String privilege) {
        this.label = label;
        this.minimumXp = minimumXp;
        this.privilege = privilege;
    }

    public String getLabel() {
        return label;
    }

    public int getMinimumXp() {
        return minimumXp;
    }

    public String getPrivilege() {
        return privilege;
    }

    public static PlayerRank fromScore(int score) {
        PlayerRank current = ROOKIE;
        for (PlayerRank rank : values()) {
            if (score >= rank.minimumXp) {
                current = rank;
            }
        }
        return current;
    }

    public static List<PlayerRank> ladder() {
        return List.of(values());
    }
}
