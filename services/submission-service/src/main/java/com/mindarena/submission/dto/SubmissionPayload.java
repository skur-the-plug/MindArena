package com.mindarena.submission.dto;

import com.mindarena.submission.model.Submission;
import java.time.LocalDateTime;

public record SubmissionPayload(Long id, Long challengeId, String challengeTitle, Long authorId, String authorName,
                                String content, String contentJson, int upvotes, boolean bestAnswer,
                                LocalDateTime createdAt) {
    public static SubmissionPayload from(Submission submission) {
        return new SubmissionPayload(
                submission.getId(),
                submission.getChallenge().getId(),
                submission.getChallenge().getTitle(),
                submission.getAuthor().getId(),
                submission.getAuthor().getFullName(),
                submission.getContent(),
                submission.getContentJson(),
                submission.getUpvotes(),
                submission.isBestAnswer(),
                submission.getCreatedAt()
        );
    }
}
