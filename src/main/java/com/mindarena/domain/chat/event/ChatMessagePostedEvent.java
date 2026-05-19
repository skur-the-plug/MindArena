package com.mindarena.domain.chat.event;

import com.mindarena.domain.chat.dto.ChatMessagePayload;

public record ChatMessagePostedEvent(String roomKey, ChatMessagePayload message) {
}
