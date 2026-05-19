package com.mindarena.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindarena.dto.ChatMessagePayload;
import com.mindarena.event.ChatMessagePostedEvent;
import com.mindarena.model.Arena;
import com.mindarena.model.Challenge;
import com.mindarena.model.ChatRoomType;
import com.mindarena.model.User;
import com.mindarena.service.ArenaService;
import com.mindarena.service.ChallengeService;
import com.mindarena.service.ChatService;
import com.mindarena.service.CurrentUserService;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final String ROOM_KEY_ATTRIBUTE = "roomKey";
    private static final int MAX_MESSAGE_LENGTH = 1000;

    private final ChatService chatService;
    private final ArenaService arenaService;
    private final ChallengeService challengeService;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;
    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(
            ChatService chatService,
            ArenaService arenaService,
            ChallengeService challengeService,
            CurrentUserService currentUserService,
            ObjectMapper objectMapper
    ) {
        this.chatService = chatService;
        this.arenaService = arenaService;
        this.challengeService = challengeService;
        this.currentUserService = currentUserService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        ChatRoom room = roomFrom(session.getUri());
        String roomKey = chatService.roomKey(room.roomType(), room.arena(), room.challenge());
        session.getAttributes().put(ROOM_KEY_ATTRIBUTE, roomKey);
        roomSessions.computeIfAbsent(roomKey, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Principal principal = session.getPrincipal();
        if (!(principal instanceof UsernamePasswordAuthenticationToken authentication)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Authentication required"));
            return;
        }

        String content = contentFrom(message.getPayload());
        if (content.isBlank() || content.length() > MAX_MESSAGE_LENGTH) {
            sendError(session, "Message must be between 1 and 1000 characters.");
            return;
        }

        ChatRoom room = roomFrom(session.getUri());
        User author = currentUserService.requireUser(authentication);
        chatService.post(author, room.roomType(), room.arena(), room.challenge(), content);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        removeSession(session);
    }

    @Async
    @EventListener
    public void onChatMessagePosted(ChatMessagePostedEvent event) throws IOException {
        broadcast(event.roomKey(), event.message());
    }

    private ChatRoom roomFrom(URI uri) {
        Map<String, String> params = UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams()
                .toSingleValueMap();
        ChatRoomType roomType = ChatRoomType.valueOf(params.getOrDefault("roomType", ChatRoomType.GLOBAL.name()));
        Arena arena = null;
        Challenge challenge = null;

        if ((roomType == ChatRoomType.ARENA || roomType == ChatRoomType.CHALLENGE) && params.containsKey("arenaId")) {
            arena = arenaService.requireArena(Long.valueOf(params.get("arenaId")));
        }
        if (roomType == ChatRoomType.CHALLENGE && params.containsKey("challengeId")) {
            challenge = challengeService.requireChallenge(Long.valueOf(params.get("challengeId")));
            if (arena == null) {
                arena = challenge.getArena();
            }
            if (!challenge.getArena().getId().equals(arena.getId())) {
                throw new IllegalArgumentException("Challenge does not belong to the selected arena");
            }
        }
        if ((roomType == ChatRoomType.ARENA && arena == null)
                || (roomType == ChatRoomType.CHALLENGE && challenge == null)) {
            throw new IllegalArgumentException("Invalid chat room");
        }

        return new ChatRoom(roomType, arena, challenge);
    }

    private String contentFrom(String payload) throws IOException {
        JsonNode node = objectMapper.readTree(payload);
        JsonNode content = node.get("content");
        return content == null ? "" : content.asText().trim();
    }

    private void broadcast(String roomKey, ChatMessagePayload payload) throws IOException {
        String json = objectMapper.writeValueAsString(Map.of("type", "message", "message", payload));
        for (WebSocketSession session : roomSessions.getOrDefault(roomKey, Collections.emptySet())) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(json));
            }
        }
    }

    private void sendError(WebSocketSession session, String error) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                "type", "error",
                "error", error
        ))));
    }

    private void removeSession(WebSocketSession session) {
        Object roomKey = session.getAttributes().get(ROOM_KEY_ATTRIBUTE);
        if (roomKey instanceof String key && roomSessions.containsKey(key)) {
            roomSessions.get(key).remove(session);
        }
    }

    private record ChatRoom(ChatRoomType roomType, Arena arena, Challenge challenge) {
    }
}
