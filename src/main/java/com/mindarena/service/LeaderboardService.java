package com.mindarena.service;

import com.mindarena.dto.LeaderboardEntry;
import com.mindarena.config.PlatformConfig;
import com.mindarena.model.Arena;
import com.mindarena.model.Challenge;
import com.mindarena.model.Role;
import com.mindarena.repository.SubmissionRepository;
import com.mindarena.repository.UserRepository;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class LeaderboardService {

    private static final int LIMIT = 10;

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final PrivilegeService privilegeService;

    public LeaderboardService(UserRepository userRepository, SubmissionRepository submissionRepository, PrivilegeService privilegeService) {
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.privilegeService = privilegeService;
    }

    @Cacheable(cacheNames = PlatformConfig.GLOBAL_LEADERBOARD_CACHE)
    public List<LeaderboardEntry> globalLeaders() {
        return userRepository.findByRoleOrderByScoreDesc(Role.USER, PageRequest.of(0, LIMIT)).stream()
                .filter(privilegeService::appearsInActiveRankings)
                .map(user -> new LeaderboardEntry(user, user.getScore()))
                .toList();
    }

    @Cacheable(cacheNames = PlatformConfig.ARENA_LEADERBOARD_CACHE, key = "#arena.id")
    public List<LeaderboardEntry> arenaLeaders(Arena arena) {
        return submissionRepository.rankByArena(arena).stream()
                .filter(entry -> privilegeService.appearsInActiveRankings(entry.getUser()))
                .limit(LIMIT)
                .toList();
    }

    @Cacheable(cacheNames = PlatformConfig.CHALLENGE_LEADERBOARD_CACHE, key = "#challenge.id")
    public List<LeaderboardEntry> challengeLeaders(Challenge challenge) {
        return submissionRepository.rankByChallenge(challenge).stream()
                .filter(entry -> privilegeService.appearsInActiveRankings(entry.getUser()))
                .limit(LIMIT)
                .toList();
    }
}
