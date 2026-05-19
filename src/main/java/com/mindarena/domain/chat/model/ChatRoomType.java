package com.mindarena.domain.chat.model;

public enum ChatRoomType {
    GLOBAL("Global"),
    ARENA("Arena"),
    CHALLENGE("Challenge");

    private final String label;

    ChatRoomType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
