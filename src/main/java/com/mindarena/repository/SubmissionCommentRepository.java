package com.mindarena.repository;

import com.mindarena.model.Submission;
import com.mindarena.model.SubmissionComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionCommentRepository extends JpaRepository<SubmissionComment, Long> {
    List<SubmissionComment> findBySubmissionOrderByCreatedAtAsc(Submission submission);
}
