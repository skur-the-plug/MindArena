package com.mindarena.service;

import com.mindarena.model.Arena;
import com.mindarena.model.Challenge;
import com.mindarena.model.ChatMessage;
import com.mindarena.model.ChatRoomType;
import com.mindarena.model.User;
import com.mindarena.repository.ChatMessageRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final int ROOM_LIMIT = 40;

    private final ChatMessageRepository chatMessageRepository;

    public ChatService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public List<ChatMessage> messages(ChatRoomType roomType, Arena arena, Challenge challenge) {
        List<ChatMessage> messages = new ArrayList<>(chatMessageRepository.findRoomMessages(
                roomType,
                arena,
                challenge,
                PageRequest.of(0, ROOM_LIMIT)
        ));
        Collections.reverse(messages);
        return messages;
    }

    public void post(User author, ChatRoomType roomType, Arena arena, Challenge challenge, String content) {
        ChatMessage message = new ChatMessage();
        message.setAuthor(author);
        message.setRoomType(roomType);
        message.setArena(arena);
        message.setChallenge(challenge);
        message.setContent(content.trim());
        chatMessageRepository.save(message);
    }
}
