package com.mindarena.submission.repository;

import com.mindarena.submission.model.Challenge;
import com.mindarena.submission.model.Submission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByChallengeOrderByUpvotesDescCreatedAtAsc(Challenge challenge);

    List<Submission> findTop50ByOrderByCreatedAtDesc();
}
