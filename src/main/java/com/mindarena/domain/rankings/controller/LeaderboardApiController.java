package com.mindarena.domain.rankings.controller;

import com.mindarena.domain.rankings.dto.LeaderboardPayload;
import com.mindarena.domain.challenges.model.Arena;
import com.mindarena.domain.challenges.model.Challenge;
import com.mindarena.domain.challenges.service.ArenaService;
import com.mindarena.domain.challenges.service.ChallengeService;
import com.mindarena.domain.rankings.service.LeaderboardService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leaderboards")
public class LeaderboardApiController {

    private final LeaderboardService leaderboardService;
    private final ArenaService arenaService;
    private final ChallengeService challengeService;

    public LeaderboardApiController(
            LeaderboardService leaderboardService,
            ArenaService arenaService,
            ChallengeService challengeService
    ) {
        this.leaderboardService = leaderboardService;
        this.arenaService = arenaService;
        this.challengeService = challengeService;
    }

    @GetMapping
    public List<LeaderboardPayload> leaders(
            @RequestParam(defaultValue = "global") String scope,
            @RequestParam(required = false) Long arenaId,
            @RequestParam(required = false) Long challengeId
    ) {
        return switch (scope) {
            case "arena" -> {
                if (arenaId == null) {
                    yield List.of();
                }
                Arena arena = arenaService.requireArena(arenaId);
                yield leaderboardService.arenaLeaders(arena).stream().map(LeaderboardPayload::from).toList();
            }
            case "challenge" -> {
                if (challengeId == null) {
                    yield List.of();
                }
                Challenge challenge = challengeService.requireChallenge(challengeId);
                yield leaderboardService.challengeLeaders(challenge).stream().map(LeaderboardPayload::from).toList();
            }
            default -> leaderboardService.globalLeaders().stream().map(LeaderboardPayload::from).toList();
        };
    }
}
