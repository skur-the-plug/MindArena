package com.mindarena.submission.repository;

import com.mindarena.submission.model.Submission;
import com.mindarena.submission.model.SubmissionComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionCommentRepository extends JpaRepository<SubmissionComment, Long> {
    List<SubmissionComment> findBySubmissionOrderByCreatedAtAsc(Submission submission);
}
