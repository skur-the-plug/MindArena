package com.mindarena.controller;

import com.mindarena.model.Arena;
import com.mindarena.model.Challenge;
import com.mindarena.model.ChatRoomType;
import com.mindarena.model.User;
import com.mindarena.service.ArenaService;
import com.mindarena.service.ChallengeService;
import com.mindarena.service.ChatService;
import com.mindarena.service.CurrentUserService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class ChatController {

    private final ChatService chatService;
    private final ArenaService arenaService;
    private final ChallengeService challengeService;
    private final CurrentUserService currentUserService;

    public ChatController(
            ChatService chatService,
            ArenaService arenaService,
            ChallengeService challengeService,
            CurrentUserService currentUserService
    ) {
        this.chatService = chatService;
        this.arenaService = arenaService;
        this.challengeService = challengeService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/chat")
    public String chat(
            @RequestParam(defaultValue = "GLOBAL") ChatRoomType roomType,
            @RequestParam(required = false) Long arenaId,
            @RequestParam(required = false) Long challengeId,
            Model model
    ) {
        List<Arena> arenas = arenaService.findAll();
        Arena arena = selectedArenaForRoom(roomType, arenaId, challengeId, arenas);
        List<Challenge> allChallenges = challengeService.activeChallenges();
        List<Challenge> roomChallenges = arena == null ? allChallenges : challengeService.activeChallengesFor(arena);
        Challenge challenge = roomType == ChatRoomType.CHALLENGE ? selectedChallenge(challengeId, roomChallenges) : null;

        model.addAttribute("roomTypes", ChatRoomType.values());
        model.addAttribute("roomType", roomType);
        model.addAttribute("arenas", arenas);
        model.addAttribute("challenges", allChallenges);
        model.addAttribute("selectedArena", arena);
        model.addAttribute("selectedChallenge", challenge);
        model.addAttribute("roomTitle", roomTitle(roomType, arena, challenge));
        model.addAttribute("messages", chatService.messages(roomType, arena, challenge));
        return "chat/index";
    }

    @PostMapping("/chat/messages")
    public String postMessage(
            @RequestParam ChatRoomType roomType,
            @RequestParam(required = false) Long arenaId,
            @RequestParam(required = false) Long challengeId,
            @RequestParam @NotBlank @Size(max = 1000) String content,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        User user = currentUserService.requireUser(authentication);
        Arena arena = (roomType == ChatRoomType.ARENA || roomType == ChatRoomType.CHALLENGE) && arenaId != null
                ? arenaService.requireArena(arenaId)
                : null;
        Challenge challenge = roomType == ChatRoomType.CHALLENGE && challengeId != null
                ? challengeService.requireChallenge(challengeId)
                : null;

        if (challenge != null && arena == null) {
            arena = challenge.getArena();
        }

        if ((roomType == ChatRoomType.ARENA && arena == null)
                || (roomType == ChatRoomType.CHALLENGE && (challenge == null || !challenge.getArena().getId().equals(arena.getId())))) {
            redirectAttributes.addFlashAttribute("error", "Choose a valid chat room.");
            return "redirect:/chat";
        }

        chatService.post(user, roomType, arena, challenge, content);
        return "redirect:" + roomUrl(roomType, arena, challenge);
    }

    private Arena selectedArena(Long arenaId, List<Arena> arenas) {
        if (arenaId != null) {
            return arenaService.requireArena(arenaId);
        }
        return arenas.isEmpty() ? null : arenas.get(0);
    }

    private Challenge selectedChallenge(Long challengeId, List<Challenge> challenges) {
        if (challengeId != null) {
            return challengeService.requireChallenge(challengeId);
        }
        return challenges.isEmpty() ? null : challenges.get(0);
    }

    private Arena selectedArenaForRoom(ChatRoomType roomType, Long arenaId, Long challengeId, List<Arena> arenas) {
        if (roomType == ChatRoomType.GLOBAL) {
            return null;
        }
        if (arenaId != null) {
            return arenaService.requireArena(arenaId);
        }
        if (roomType == ChatRoomType.CHALLENGE && challengeId != null) {
            return challengeService.requireChallenge(challengeId).getArena();
        }
        return selectedArena(null, arenas);
    }

    private String roomUrl(ChatRoomType roomType, Arena arena, Challenge challenge) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/chat")
                .queryParam("roomType", roomType.name());
        if (arena != null) {
            builder.queryParam("arenaId", arena.getId());
        }
        if (challenge != null) {
            builder.queryParam("challengeId", challenge.getId());
        }
        return builder.toUriString();
    }

    private String roomTitle(ChatRoomType roomType, Arena arena, Challenge challenge) {
        if (roomType == ChatRoomType.ARENA && arena != null) {
            return arena.getName();
        }
        if (roomType == ChatRoomType.CHALLENGE && challenge != null) {
            return challenge.getTitle();
        }
        return "Global chat";
    }
}
