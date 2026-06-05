package com.mindarena.challenge.dto;

import com.mindarena.challenge.model.Arena;

public record ArenaPayload(
        Long id,
        String name,
        String description,
        String color
) {
    public static ArenaPayload from(Arena arena) {
        return new ArenaPayload(arena.getId(), arena.getName(), arena.getDescription(), arena.getColor());
    }
}
