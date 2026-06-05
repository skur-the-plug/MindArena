package com.mindarena.challenge.controller;

import com.mindarena.challenge.dto.ArenaPayload;
import com.mindarena.challenge.dto.ChallengePayload;
import com.mindarena.challenge.dto.ChallengeRequest;
import com.mindarena.challenge.repository.ArenaRepository;
import com.mindarena.challenge.service.ChallengeCatalogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeApiController {

    private final ArenaRepository arenaRepository;
    private final ChallengeCatalogService challengeService;

    public ChallengeApiController(ArenaRepository arenaRepository, ChallengeCatalogService challengeService) {
        this.arenaRepository = arenaRepository;
        this.challengeService = challengeService;
    }

    @GetMapping("/arenas")
    public List<ArenaPayload> arenas() {
        return arenaRepository.findAll().stream().map(ArenaPayload::from).toList();
    }

    @GetMapping
    public List<ChallengePayload> challenges(
            @RequestParam(required = false) Long arenaId,
            @RequestParam(defaultValue = "deadline") String sort,
            @RequestParam(defaultValue = "All") String difficulty
    ) {
        var challenges = arenaId == null
                ? challengeService.activeChallenges(sort, difficulty)
                : challengeService.activeChallengesFor(arenaId, sort, difficulty);
        return challenges.stream().map(ChallengePayload::from).toList();
    }

    @GetMapping("/{id}")
    public ChallengePayload challenge(@PathVariable Long id) {
        return ChallengePayload.from(challengeService.requireChallenge(id));
    }

    @PostMapping
    public ChallengePayload create(@RequestBody ChallengeRequest request) {
        return ChallengePayload.from(challengeService.create(request));
    }

    @PutMapping("/{id}")
    public ChallengePayload update(@PathVariable Long id, @RequestBody ChallengeRequest request) {
        return ChallengePayload.from(challengeService.update(id, request));
    }

    @PostMapping("/{id}/toggle")
    public ChallengePayload toggle(@PathVariable Long id) {
        return ChallengePayload.from(challengeService.toggle(id));
    }
}
