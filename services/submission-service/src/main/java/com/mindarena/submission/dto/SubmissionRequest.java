package com.mindarena.submission.dto;

public record SubmissionRequest(Long challengeId, Long authorId, String content, String contentJson) {
}
