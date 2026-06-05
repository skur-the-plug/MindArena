package com.mindarena.ranking.service;

import com.mindarena.ranking.dto.LeaderboardEntry;
import com.mindarena.ranking.model.Arena;
import com.mindarena.ranking.model.Challenge;
import com.mindarena.ranking.model.Role;
import com.mindarena.ranking.repository.SubmissionRepository;
import com.mindarena.ranking.repository.UserRepository;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class LeaderboardService {

    private static final int LIMIT = 10;

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    public LeaderboardService(UserRepository userRepository, SubmissionRepository submissionRepository) {
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
    }

    @Cacheable(cacheNames = "globalLeaderboard")
    public List<LeaderboardEntry> globalLeaders() {
        return userRepository.findByRoleOrderByScoreDesc(Role.USER, PageRequest.of(0, LIMIT)).stream()
                .map(user -> new LeaderboardEntry(user, user.getScore()))
                .toList();
    }

    @Cacheable(cacheNames = "arenaLeaderboard", key = "#arena.id")
    public List<LeaderboardEntry> arenaLeaders(Arena arena) {
        return submissionRepository.rankByArena(arena).stream()
                .limit(LIMIT)
                .toList();
    }

    @Cacheable(cacheNames = "challengeLeaderboard", key = "#challenge.id")
    public List<LeaderboardEntry> challengeLeaders(Challenge challenge) {
        return submissionRepository.rankByChallenge(challenge).stream()
                .limit(LIMIT)
                .toList();
    }
}
