package com.mindarena.submission.service;

import com.mindarena.submission.dto.CommentRequest;
import com.mindarena.submission.dto.SubmissionRequest;
import com.mindarena.submission.model.Submission;
import com.mindarena.submission.model.SubmissionComment;
import com.mindarena.submission.model.Vote;
import com.mindarena.submission.repository.ChallengeRepository;
import com.mindarena.submission.repository.SubmissionCommentRepository;
import com.mindarena.submission.repository.SubmissionRepository;
import com.mindarena.submission.repository.UserRepository;
import com.mindarena.submission.repository.VoteRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmissionApiService {
    private final SubmissionRepository submissionRepository;
    private final SubmissionCommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    public SubmissionApiService(SubmissionRepository submissionRepository, SubmissionCommentRepository commentRepository,
                                VoteRepository voteRepository, ChallengeRepository challengeRepository,
                                UserRepository userRepository) {
        this.submissionRepository = submissionRepository;
        this.commentRepository = commentRepository;
        this.voteRepository = voteRepository;
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
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
    public Submission submit(SubmissionRequest request) {
        Submission submission = new Submission();
        submission.setChallenge(challengeRepository.findById(request.challengeId()).orElseThrow(() -> new IllegalArgumentException("Challenge not found")));
        submission.setAuthor(userRepository.findById(request.authorId()).orElseThrow(() -> new IllegalArgumentException("User not found")));
        submission.setContent(request.content());
        submission.setContentJson(request.contentJson());
        return submissionRepository.save(submission);
    }

    @Transactional
    public SubmissionComment comment(Long submissionId, CommentRequest request) {
        SubmissionComment comment = new SubmissionComment();
        comment.setSubmission(requireSubmission(submissionId));
        comment.setAuthor(userRepository.findById(request.authorId()).orElseThrow(() -> new IllegalArgumentException("User not found")));
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
