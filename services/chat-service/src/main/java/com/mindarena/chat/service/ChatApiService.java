package com.mindarena.chat.service;

import com.mindarena.chat.dto.PostMessageRequest;
import com.mindarena.chat.model.Arena;
import com.mindarena.chat.model.Challenge;
import com.mindarena.chat.model.ChatMessage;
import com.mindarena.chat.model.ChatRoomType;
import com.mindarena.chat.repository.ArenaRepository;
import com.mindarena.chat.repository.ChallengeRepository;
import com.mindarena.chat.repository.ChatMessageRepository;
import com.mindarena.chat.repository.UserRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatApiService {
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ArenaRepository arenaRepository;
    private final ChallengeRepository challengeRepository;

    public ChatApiService(ChatMessageRepository messageRepository, UserRepository userRepository,
                          ArenaRepository arenaRepository, ChallengeRepository challengeRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.arenaRepository = arenaRepository;
        this.challengeRepository = challengeRepository;
    }

    public List<ChatMessage> messages(String roomType, Long arenaId, Long challengeId) {
        ChatRoomType type = ChatRoomType.valueOf(roomType);
        Arena arena = arenaId == null ? null : arenaRepository.findById(arenaId).orElseThrow(() -> new IllegalArgumentException("Arena not found"));
        Challenge challenge = challengeId == null ? null : challengeRepository.findById(challengeId).orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
        return messageRepository.findRoomMessages(type, arena, challenge, PageRequest.of(0, 50));
    }

    @Transactional
    public ChatMessage post(PostMessageRequest request) {
        ChatMessage message = new ChatMessage();
        message.setRoomType(ChatRoomType.valueOf(request.roomType()));
        message.setAuthor(userRepository.findById(request.authorId()).orElseThrow(() -> new IllegalArgumentException("User not found")));
        if (request.arenaId() != null) {
            message.setArena(arenaRepository.findById(request.arenaId()).orElseThrow(() -> new IllegalArgumentException("Arena not found")));
        }
        if (request.challengeId() != null) {
            message.setChallenge(challengeRepository.findById(request.challengeId()).orElseThrow(() -> new IllegalArgumentException("Challenge not found")));
        }
        message.setContent(request.content());
        return messageRepository.save(message);
    }
}
