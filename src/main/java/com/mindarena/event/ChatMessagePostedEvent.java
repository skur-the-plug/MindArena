package com.mindarena.event;

import com.mindarena.dto.ChatMessagePayload;

public record ChatMessagePostedEvent(String roomKey, ChatMessagePayload message) {
}
