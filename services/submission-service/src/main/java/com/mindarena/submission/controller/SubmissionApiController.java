package com.mindarena.submission.controller;

import com.mindarena.submission.dto.CommentPayload;
import com.mindarena.submission.dto.CommentRequest;
import com.mindarena.submission.dto.SubmissionPayload;
import com.mindarena.submission.dto.SubmissionRequest;
import com.mindarena.submission.service.SubmissionApiService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionApiController {
    private final SubmissionApiService submissionService;

    public SubmissionApiController(SubmissionApiService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping
    public List<SubmissionPayload> submissions(@RequestParam(required = false) Long challengeId) {
        return submissionService.forChallenge(challengeId).stream().map(SubmissionPayload::from).toList();
    }

    @GetMapping("/{id}")
    public SubmissionPayload submission(@PathVariable Long id) {
        return SubmissionPayload.from(submissionService.requireSubmission(id));
    }

    @PostMapping
    public SubmissionPayload submit(@RequestBody SubmissionRequest request) {
        return SubmissionPayload.from(submissionService.submit(request));
    }

    @GetMapping("/{id}/comments")
    public List<CommentPayload> comments(@PathVariable Long id) {
        return submissionService.comments(id).stream().map(CommentPayload::from).toList();
    }

    @PostMapping("/{id}/comments")
    public CommentPayload comment(@PathVariable Long id, @RequestBody CommentRequest request) {
        return CommentPayload.from(submissionService.comment(id, request));
    }

    @PostMapping("/{id}/upvote")
    public SubmissionPayload upvote(@PathVariable Long id, @RequestParam Long voterId) {
        return SubmissionPayload.from(submissionService.upvote(id, voterId));
    }
}
