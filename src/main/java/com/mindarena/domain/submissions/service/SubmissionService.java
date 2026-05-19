package com.mindarena.domain.submissions.service;

import com.mindarena.domain.identity.service.PrivilegeService;
import com.mindarena.domain.notifications.service.NotificationService;
import com.mindarena.domain.rankings.event.LeaderboardChangedEvent;
import com.mindarena.domain.challenges.model.Challenge;
import com.mindarena.domain.notifications.model.NotificationType;
import com.mindarena.domain.submissions.model.Submission;
import com.mindarena.domain.submissions.model.SubmissionComment;
import com.mindarena.domain.identity.model.User;
import com.mindarena.domain.submissions.model.Vote;
import com.mindarena.domain.submissions.repository.SubmissionCommentRepository;
import com.mindarena.domain.submissions.repository.SubmissionRepository;
import com.mindarena.domain.submissions.repository.VoteRepository;
import java.util.List;
import java.util.Comparator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmissionService {

    private static final int SUBMISSION_POINTS = 10;
    private static final int OWNER_RECEIVED_SUBMISSION_POINTS = 5;

    private final SubmissionRepository submissionRepository;
    private final SubmissionCommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            SubmissionCommentRepository commentRepository,
            VoteRepository voteRepository,
            NotificationService notificationService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.submissionRepository = submissionRepository;
        this.commentRepository = commentRepository;
        this.voteRepository = voteRepository;
        this.notificationService = notificationService;
        this.eventPublisher = eventPublisher;
    }

    public List<Submission> forChallenge(Challenge challenge) {
        return submissionRepository.findByChallengeOrderByUpvotesDescCreatedAtAsc(challenge);
    }

    public List<Submission> forChallenge(Challenge challenge, String sort, String filter) {
        Comparator<Submission> comparator = switch (sort == null ? "" : sort) {
            case "newest" -> Comparator.comparing(Submission::getCreatedAt).reversed();
            case "oldest" -> Comparator.comparing(Submission::getCreatedAt);
            case "best" -> Comparator.comparing(Submission::isBestAnswer).reversed()
                    .thenComparing(Submission::getUpvotes, Comparator.reverseOrder());
            default -> Comparator.comparing(Submission::getUpvotes, Comparator.reverseOrder())
                    .thenComparing(Submission::getCreatedAt);
        };
        return forChallenge(challenge).stream()
                .filter(submission -> filter == null || filter.isBlank() || "All".equalsIgnoreCase(filter)
                        || ("Best".equalsIgnoreCase(filter) && submission.isBestAnswer()))
                .sorted(comparator)
                .toList();
    }

    public Submission requireSubmission(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
    }

    public List<SubmissionComment> commentsFor(Submission submission) {
        return commentRepository.findBySubmissionOrderByCreatedAtAsc(submission);
    }

    @Transactional
    public Submission submit(User author, Challenge challenge, String content, String contentJson) {
        Submission submission = new Submission();
        submission.setAuthor(author);
        submission.setChallenge(challenge);
        submission.setContent(content);
        submission.setContentJson(contentJson);
        Submission saved = submissionRepository.save(submission);
        notificationService.awardXp(author, SUBMISSION_POINTS, "You submitted an answer in " + challenge.getArena().getName() + ".", "/submissions/" + saved.getId());
        User creator = challenge.getCreator();
        if (creator != null && !creator.getId().equals(author.getId())) {
            notificationService.awardXp(creator, OWNER_RECEIVED_SUBMISSION_POINTS, author.getFullName() + " submitted an answer to your challenge: " + challenge.getTitle() + ".", "/submissions/" + saved.getId());
        }
        publishLeaderboardChanged(challenge);
        return saved;
    }

    @Transactional
    public SubmissionComment comment(User author, Submission submission, String content) {
        SubmissionComment comment = new SubmissionComment();
        comment.setAuthor(author);
        comment.setSubmission(submission);
        comment.setContent(content);
        SubmissionComment saved = commentRepository.save(comment);
        User submissionAuthor = submission.getAuthor();
        if (submissionAuthor != null && !submissionAuthor.getId().equals(author.getId())) {
            notificationService.notify(submissionAuthor, NotificationType.SUBMISSION, author.getFullName() + " commented on your submission for: " + submission.getChallenge().getTitle() + ".", "/submissions/" + submission.getId());
        }
        User creator = submission.getChallenge().getCreator();
        if (creator != null && !creator.getId().equals(author.getId()) && !creator.getId().equals(submissionAuthor.getId())) {
            notificationService.notify(creator, NotificationType.SUBMISSION, author.getFullName() + " commented on a submission in your challenge: " + submission.getChallenge().getTitle() + ".", "/submissions/" + submission.getId());
        }
        return saved;
    }

    @Transactional
    public boolean upvote(User voter, Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        if (submission.getAuthor().getId().equals(voter.getId())
                || voteRepository.existsByVoterAndSubmission(voter, submission)) {
            return false;
        }

        submission.addUpvote();
        voteRepository.save(new Vote(voter, submission));
        submissionRepository.save(submission);
        int points = upvotePoints(submission.getChallenge());
        notificationService.awardXp(submission.getAuthor(), points, "Your " + submission.getChallenge().getDifficulty() + " submission in " + submission.getChallenge().getArena().getName() + " received an upvote.", "/submissions/" + submission.getId());
        publishLeaderboardChanged(submission.getChallenge());
        return true;
    }

    @Transactional
    public Submission selectBestAnswer(User actor, Long submissionId, PrivilegeService privilegeService) {
        Submission selected = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        if (!privilegeService.canSelectBestAnswer(actor, selected.getChallenge())) {
            throw new IllegalStateException("You cannot manage this challenge.");
        }

        for (Submission previous : submissionRepository.findByChallengeAndBestAnswerTrue(selected.getChallenge())) {
            if (!previous.getId().equals(selected.getId())) {
                previous.setBestAnswer(false);
                submissionRepository.save(previous);
            }
        }

        if (!selected.isBestAnswer()) {
            selected.setBestAnswer(true);
            int points = bestAnswerPoints(selected.getChallenge());
            notificationService.awardXp(selected.getAuthor(), points, "Your " + selected.getChallenge().getDifficulty() + " submission for " + selected.getChallenge().getTitle() + " was selected as the best answer.", "/submissions/" + selected.getId());
        }

        Submission saved = submissionRepository.save(selected);
        publishLeaderboardChanged(saved.getChallenge());
        return saved;
    }

    public int upvotePoints(Challenge challenge) {
        return switch (normalizedDifficulty(challenge)) {
            case "Beginner" -> 3;
            case "Advanced" -> 8;
            case "Expert" -> 12;
            default -> 5;
        };
    }

    public int bestAnswerPoints(Challenge challenge) {
        return switch (normalizedDifficulty(challenge)) {
            case "Beginner" -> 15;
            case "Advanced" -> 40;
            case "Expert" -> 60;
            default -> 25;
        };
    }

    private String normalizedDifficulty(Challenge challenge) {
        if (challenge.getDifficulty() == null || challenge.getDifficulty().isBlank()) {
            return "Intermediate";
        }
        return challenge.getDifficulty();
    }

    private void publishLeaderboardChanged(Challenge challenge) {
        eventPublisher.publishEvent(new LeaderboardChangedEvent(
                challenge.getArena().getId(),
                challenge.getId()
        ));
    }
}
