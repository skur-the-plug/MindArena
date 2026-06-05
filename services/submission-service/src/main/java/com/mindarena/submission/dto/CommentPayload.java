package com.mindarena.submission.dto;

import com.mindarena.submission.model.SubmissionComment;
import java.time.LocalDateTime;

public record CommentPayload(Long id, Long submissionId, Long authorId, String authorName, String content,
                             LocalDateTime createdAt) {
    public static CommentPayload from(SubmissionComment comment) {
        return new CommentPayload(
                comment.getId(),
                comment.getSubmission().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getFullName(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
