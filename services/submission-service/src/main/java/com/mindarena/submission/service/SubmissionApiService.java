package com.mindarena.submission.service;

import com.mindarena.submission.client.ChallengePolicyResponse;
import com.mindarena.submission.client.ChallengeServiceClient;
import com.mindarena.submission.client.IdentityServiceClient;
import com.mindarena.submission.client.UserVerificationResponse;
import com.mindarena.submission.dto.CommentRequest;
import com.mindarena.submission.dto.SubmissionRequest;
import com.mindarena.submission.model.Arena;
import com.mindarena.submission.model.Challenge;
import com.mindarena.submission.model.Submission;
import com.mindarena.submission.model.SubmissionComment;
import com.mindarena.submission.model.User;
import com.mindarena.submission.model.Vote;
import com.mindarena.submission.repository.ArenaRepository;
import com.mindarena.submission.repository.ChallengeRepository;
import com.mindarena.submission.repository.SubmissionCommentRepository;
import com.mindarena.submission.repository.SubmissionRepository;
import com.mindarena.submission.repository.UserRepository;
import com.mindarena.submission.repository.VoteRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmissionApiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubmissionApiService.class);

    private final SubmissionRepository submissionRepository;
    private final SubmissionCommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final ArenaRepository arenaRepository;
    private final IdentityServiceClient identityServiceClient;
    private final ChallengeServiceClient challengeServiceClient;

    public SubmissionApiService(SubmissionRepository submissionRepository, SubmissionCommentRepository commentRepository,
                                VoteRepository voteRepository, ChallengeRepository challengeRepository,
                                UserRepository userRepository, ArenaRepository arenaRepository,
                                IdentityServiceClient identityServiceClient,
                                ChallengeServiceClient challengeServiceClient) {
        this.submissionRepository = submissionRepository;
        this.commentRepository = commentRepository;
        this.voteRepository = voteRepository;
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
        this.arenaRepository = arenaRepository;
        this.identityServiceClient = identityServiceClient;
        this.challengeServiceClient = challengeServiceClient;
    }

    public Submission requireSubmission(Long id) {
        return submissionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Submission not found"));
    }

    public List<Submission> forChallenge(Long challengeId) {
        if (challengeId == null) {
            return submissionRepository.findTop50ByOrderByCreatedAtDesc();
        }
        return submissionRepository.findByChallengeOrderByUpvotesDescCreatedAtAsc(
                challengeRepository.findById(challengeId).orElseThrow(() -> new IllegalArgumentException("Challenge not found")));
    }

    @Transactional
    public Submission submit(SubmissionRequest request, Long authorId) {
        if ((request.content() == null || request.content().isBlank())
                && (request.contentJson() == null || request.contentJson().isBlank())) {
            throw new IllegalArgumentException("Submission content is required");
        }
        LOGGER.info("Starting secure submission flow for user {} and challenge {}", authorId, request.challengeId());
        UserVerificationResponse verifiedUser = identityServiceClient.verifyUser(authorId);
        ChallengePolicyResponse verifiedChallenge = challengeServiceClient.validateSubmission(request.challengeId());

        User author = userRepository.findById(authorId).orElseGet(User::new);
        author.setId(verifiedUser.userId());
        author.setFullName(verifiedUser.fullName());
        author = userRepository.save(author);

        Arena arena = arenaRepository.findById(verifiedChallenge.arenaId()).orElseGet(Arena::new);
        arena.setId(verifiedChallenge.arenaId());
        arena.setName(verifiedChallenge.arenaName());
        arena = arenaRepository.save(arena);

        Challenge challenge = challengeRepository.findById(verifiedChallenge.challengeId()).orElseGet(Challenge::new);
        challenge.setId(verifiedChallenge.challengeId());
        challenge.setTitle(verifiedChallenge.title());
        challenge.setDifficulty(verifiedChallenge.difficulty());
        challenge.setArena(arena);
        challenge = challengeRepository.save(challenge);

        Submission submission = new Submission();
        submission.setChallenge(challenge);
        submission.setAuthor(author);
        submission.setContent(request.content());
        submission.setContentJson(request.contentJson());
        Submission saved = submissionRepository.save(submission);
        LOGGER.info("Secure submission flow persisted submission {}", saved.getId());
        return saved;
    }

    @Transactional
    public SubmissionComment comment(Long submissionId, CommentRequest request, Long authorId) {
        SubmissionComment comment = new SubmissionComment();
        comment.setSubmission(requireSubmission(submissionId));
        comment.setAuthor(userRepository.findById(authorId).orElseThrow(() -> new IllegalArgumentException("User not found")));
        comment.setContent(request.content());
        return commentRepository.save(comment);
    }

    public List<SubmissionComment> comments(Long submissionId) {
        return commentRepository.findBySubmissionOrderByCreatedAtAsc(requireSubmission(submissionId));
    }

    @Transactional
    public Submission upvote(Long submissionId, Long voterId) {
        Submission submission = requireSubmission(submissionId);
        var voter = userRepository.findById(voterId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!submission.getAuthor().getId().equals(voterId) && !voteRepository.existsByVoterAndSubmission(voter, submission)) {
            submission.setUpvotes(submission.getUpvotes() + 1);
            voteRepository.save(new Vote(voter, submission));
        }
        return submissionRepository.save(submission);
    }
}
