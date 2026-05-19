package com.mindarena.domain.challenges.service;

import com.mindarena.domain.notifications.service.NotificationService;
import com.mindarena.domain.challenges.model.Arena;
import com.mindarena.domain.challenges.model.ArenaMembership;
import com.mindarena.domain.challenges.model.Challenge;
import com.mindarena.domain.notifications.model.NotificationType;
import com.mindarena.domain.challenges.model.TemplateType;
import com.mindarena.domain.identity.model.User;
import com.mindarena.domain.challenges.repository.ArenaMembershipRepository;
import com.mindarena.domain.challenges.repository.ChallengeRepository;
import java.util.List;
import java.util.Comparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChallengeService {

    private static final int CREATE_CHALLENGE_POINTS = 15;

    private final ChallengeRepository challengeRepository;
    private final NotificationService notificationService;
    private final ArenaMembershipRepository membershipRepository;

    public ChallengeService(
            ChallengeRepository challengeRepository,
            NotificationService notificationService,
            ArenaMembershipRepository membershipRepository
    ) {
        this.challengeRepository = challengeRepository;
        this.notificationService = notificationService;
        this.membershipRepository = membershipRepository;
    }

    public List<Challenge> activeChallenges() {
        return challengeRepository.findByActiveTrueOrderByDeadlineAsc();
    }

    public List<Challenge> activeChallenges(String sort, String difficulty) {
        return sortedAndFiltered(activeChallenges(), sort, difficulty);
    }

    public List<Challenge> archivedChallenges() {
        return challengeRepository.findByActiveFalseOrderByDeadlineDesc();
    }

    public List<Challenge> allChallenges() {
        return challengeRepository.findAll();
    }

    public List<Challenge> activeChallengesFor(Arena arena) {
        return challengeRepository.findByArenaAndActiveTrueOrderByDeadlineAsc(arena);
    }

    public List<Challenge> activeChallengesFor(Arena arena, String sort, String difficulty) {
        return sortedAndFiltered(activeChallengesFor(arena), sort, difficulty);
    }

    public Challenge requireChallenge(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
    }

    public Challenge save(Challenge challenge) {
        return challengeRepository.save(challenge);
    }

    @Transactional
    public Challenge createChallenge(User creator, Arena arena, String title, String brief, String difficulty, TemplateType templateType, String templateName, String templateBody, java.time.LocalDateTime deadline) {
        Challenge challenge = new Challenge();
        challenge.setCreator(creator);
        challenge.setArena(arena);
        challenge.setTitle(title);
        challenge.setBrief(brief);
        challenge.setDifficulty(difficulty);
        challenge.setTemplateType(templateType);
        challenge.setSubmissionTemplateName(templateName);
        challenge.setSubmissionTemplateBody(templateBody);
        challenge.setDeadline(deadline);
        Challenge saved = challengeRepository.save(challenge);
        notificationService.awardXp(creator, CREATE_CHALLENGE_POINTS, "You created a challenge in " + arena.getName() + ".", "/challenges/" + saved.getId());
        for (ArenaMembership membership : membershipRepository.findByArena(arena)) {
            User member = membership.getUser();
            if (!member.getId().equals(creator.getId())) {
                notificationService.notify(member, NotificationType.CHALLENGE, "A new challenge was added in " + arena.getName() + ": " + title + ".", "/challenges/" + saved.getId());
            }
        }
        return saved;
    }

    public Challenge updateChallenge(Challenge challenge, String title, String brief, String difficulty, TemplateType templateType, String templateName, String templateBody, java.time.LocalDateTime deadline) {
        challenge.setTitle(title);
        challenge.setBrief(brief);
        challenge.setDifficulty(difficulty);
        challenge.setTemplateType(templateType);
        challenge.setSubmissionTemplateName(templateName);
        challenge.setSubmissionTemplateBody(templateBody);
        challenge.setDeadline(deadline);
        return challengeRepository.save(challenge);
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
