package com.mindarena.controller;

import com.mindarena.dto.LeaderboardEntry;
import com.mindarena.model.Arena;
import com.mindarena.model.Challenge;
import com.mindarena.model.PlayerRank;
import com.mindarena.service.ArenaService;
import com.mindarena.service.ChallengeService;
import com.mindarena.service.LeaderboardService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final ArenaService arenaService;
    private final ChallengeService challengeService;

    public LeaderboardController(
            LeaderboardService leaderboardService,
            ArenaService arenaService,
            ChallengeService challengeService
    ) {
        this.leaderboardService = leaderboardService;
        this.arenaService = arenaService;
        this.challengeService = challengeService;
    }

    @GetMapping("/leaderboard")
    public String leaderboard(
            @RequestParam(defaultValue = "global") String scope,
            @RequestParam(required = false) Long arenaId,
            @RequestParam(required = false) Long challengeId,
            Model model
    ) {
        List<Arena> arenas = arenaService.findAll();
        List<Challenge> challenges = challengeService.activeChallenges();
        Arena selectedArena = arenaId != null ? arenaService.requireArena(arenaId) : firstOrNull(arenas);
        Challenge selectedChallenge = challengeId != null ? challengeService.requireChallenge(challengeId) : firstOrNull(challenges);

        List<LeaderboardEntry> leaders = switch (scope) {
            case "arena" -> selectedArena == null ? List.of() : leaderboardService.arenaLeaders(selectedArena);
            case "challenge" -> selectedChallenge == null ? List.of() : leaderboardService.challengeLeaders(selectedChallenge);
            default -> leaderboardService.globalLeaders();
        };

        String boardTitle = switch (scope) {
            case "arena" -> selectedArena == null ? "Arena Board" : selectedArena.getName();
            case "challenge" -> selectedChallenge == null ? "Challenge Board" : selectedChallenge.getTitle();
            default -> "Global";
        };

        model.addAttribute("scope", scope);
        model.addAttribute("leaders", leaders);
        model.addAttribute("arenas", arenas);
        model.addAttribute("challenges", challenges);
        model.addAttribute("selectedArena", selectedArena);
        model.addAttribute("selectedChallenge", selectedChallenge);
        model.addAttribute("boardTitle", boardTitle);
        model.addAttribute("rankLadder", PlayerRank.ladder());
        return "leaderboard";
    }

    private <T> T firstOrNull(List<T> items) {
        return items.isEmpty() ? null : items.get(0);
    }
}
