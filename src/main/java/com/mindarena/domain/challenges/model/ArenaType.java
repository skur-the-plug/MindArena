package com.mindarena.domain.challenges.model;

public enum ArenaType {
    CODING,
    BUSINESS,
    CREATIVITY,
    DEBATE;

    public static ArenaType fromArenaName(String arenaName) {
        if (arenaName == null) {
            return CODING;
        }
        return switch (arenaName) {
            case "Business Arena" -> BUSINESS;
            case "Creativity Arena" -> CREATIVITY;
            case "Debate Arena" -> DEBATE;
            default -> CODING;
        };
    }
}
