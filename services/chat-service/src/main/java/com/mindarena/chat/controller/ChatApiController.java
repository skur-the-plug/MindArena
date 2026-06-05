package com.mindarena.chat.controller;

import com.mindarena.chat.dto.ChatMessagePayload;
import com.mindarena.chat.dto.PostMessageRequest;
import com.mindarena.chat.service.ChatApiService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatApiController {
    private final ChatApiService chatService;

    public ChatApiController(ChatApiService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/messages")
    public List<ChatMessagePayload> messages(@RequestParam(defaultValue = "GLOBAL") String roomType,
                                             @RequestParam(required = false) Long arenaId,
                                             @RequestParam(required = false) Long challengeId) {
        return chatService.messages(roomType, arenaId, challengeId).stream().map(ChatMessagePayload::from).toList();
    }

    @PostMapping("/messages")
    public ChatMessagePayload post(@RequestBody PostMessageRequest request) {
        return ChatMessagePayload.from(chatService.post(request));
    }
}
