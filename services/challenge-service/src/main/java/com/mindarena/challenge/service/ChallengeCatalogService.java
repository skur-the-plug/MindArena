package com.mindarena.challenge.service;

import com.mindarena.challenge.dto.ChallengeRequest;
import com.mindarena.challenge.model.Arena;
import com.mindarena.challenge.model.Challenge;
import com.mindarena.challenge.model.TemplateType;
import com.mindarena.challenge.repository.ArenaRepository;
import com.mindarena.challenge.repository.ChallengeRepository;
import com.mindarena.challenge.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChallengeCatalogService {

    private final ChallengeRepository challengeRepository;
    private final ArenaRepository arenaRepository;
    private final UserRepository userRepository;

    public ChallengeCatalogService(
            ChallengeRepository challengeRepository,
            ArenaRepository arenaRepository,
            UserRepository userRepository
    ) {
        this.challengeRepository = challengeRepository;
        this.arenaRepository = arenaRepository;
        this.userRepository = userRepository;
    }

    public Challenge requireChallenge(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
    }

    public List<Challenge> activeChallenges(String sort, String difficulty) {
        return sortedAndFiltered(challengeRepository.findByActiveTrueOrderByDeadlineAsc(), sort, difficulty);
    }

    public List<Challenge> activeChallengesFor(Long arenaId, String sort, String difficulty) {
        Arena arena = arenaRepository.findById(arenaId)
                .orElseThrow(() -> new IllegalArgumentException("Arena not found"));
        return sortedAndFiltered(challengeRepository.findByArenaAndActiveTrueOrderByDeadlineAsc(arena), sort, difficulty);
    }

    @Transactional
    public Challenge create(ChallengeRequest request) {
        Challenge challenge = new Challenge();
        apply(challenge, request);
        return challengeRepository.save(challenge);
    }

    @Transactional
    public Challenge update(Long id, ChallengeRequest request) {
        Challenge challenge = requireChallenge(id);
        apply(challenge, request);
        return challengeRepository.save(challenge);
    }

    @Transactional
    public Challenge toggle(Long id) {
        Challenge challenge = requireChallenge(id);
        challenge.setActive(!challenge.isActive());
        return challengeRepository.save(challenge);
    }

    private void apply(Challenge challenge, ChallengeRequest request) {
        challenge.setArena(arenaRepository.findById(request.arenaId())
                .orElseThrow(() -> new IllegalArgumentException("Arena not found")));
        if (request.creatorId() != null) {
            challenge.setCreator(userRepository.findById(request.creatorId())
                    .orElseThrow(() -> new IllegalArgumentException("Creator not found")));
        }
        challenge.setTitle(request.title());
        challenge.setBrief(request.brief());
        challenge.setDifficulty(request.difficulty() == null || request.difficulty().isBlank() ? "Intermediate" : request.difficulty());
        challenge.setTemplateType(request.templateType() == null || request.templateType().isBlank() ? null : TemplateType.valueOf(request.templateType()));
        challenge.setSubmissionTemplateName(request.submissionTemplateName());
        challenge.setSubmissionTemplateBody(request.submissionTemplateBody());
        challenge.setDeadline(request.deadline());
    }

    private List<Challenge> sortedAndFiltered(List<Challenge> challenges, String sort, String difficulty) {
        Comparator<Challenge> comparator = switch (sort == null ? "" : sort) {
            case "newest" -> Comparator.comparing(Challenge::getId, Comparator.nullsLast(Long::compareTo)).reversed();
            case "title" -> Comparator.comparing(Challenge::getTitle, String.CASE_INSENSITIVE_ORDER);
            case "difficulty" -> Comparator.comparingInt(this::difficultyWeight).reversed()
                    .thenComparing(Challenge::getDeadline, Comparator.nullsLast(java.time.LocalDateTime::compareTo));
            default -> Comparator.comparing(Challenge::getDeadline, Comparator.nullsLast(java.time.LocalDateTime::compareTo));
        };
        return challenges.stream()
                .filter(challenge -> difficulty == null || difficulty.isBlank() || "All".equalsIgnoreCase(difficulty) || difficulty.equalsIgnoreCase(challenge.getDifficulty()))
                .sorted(comparator)
                .toList();
    }

    private int difficultyWeight(Challenge challenge) {
        return switch (challenge.getDifficulty() == null ? "Intermediate" : challenge.getDifficulty()) {
            case "Beginner" -> 1;
            case "Advanced" -> 3;
            case "Expert" -> 4;
            default -> 2;
        };
    }
}
