package com.mindarena.domain.submissions.repository;

import com.mindarena.domain.submissions.model.Submission;
import com.mindarena.domain.submissions.model.SubmissionComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionCommentRepository extends JpaRepository<SubmissionComment, Long> {
    List<SubmissionComment> findBySubmissionOrderByCreatedAtAsc(Submission submission);
}
