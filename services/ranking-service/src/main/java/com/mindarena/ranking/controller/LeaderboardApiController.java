package com.mindarena.ranking.controller;

import com.mindarena.ranking.dto.LeaderboardPayload;
import com.mindarena.ranking.model.Arena;
import com.mindarena.ranking.model.Challenge;
import com.mindarena.ranking.repository.ArenaRepository;
import com.mindarena.ranking.repository.ChallengeRepository;
import com.mindarena.ranking.service.LeaderboardService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leaderboards")
public class LeaderboardApiController {

    private final LeaderboardService leaderboardService;
    private final ArenaRepository arenaRepository;
    private final ChallengeRepository challengeRepository;

    public LeaderboardApiController(
            LeaderboardService leaderboardService,
            ArenaRepository arenaRepository,
            ChallengeRepository challengeRepository
    ) {
        this.leaderboardService = leaderboardService;
        this.arenaRepository = arenaRepository;
        this.challengeRepository = challengeRepository;
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
                Arena arena = arenaRepository.findById(arenaId)
                        .orElseThrow(() -> new IllegalArgumentException("Arena not found"));
                yield leaderboardService.arenaLeaders(arena).stream().map(LeaderboardPayload::from).toList();
            }
            case "challenge" -> {
                if (challengeId == null) {
                    yield List.of();
                }
                Challenge challenge = challengeRepository.findById(challengeId)
                        .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
                yield leaderboardService.challengeLeaders(challenge).stream().map(LeaderboardPayload::from).toList();
            }
            default -> leaderboardService.globalLeaders().stream().map(LeaderboardPayload::from).toList();
        };
    }
}
