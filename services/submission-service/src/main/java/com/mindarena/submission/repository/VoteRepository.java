package com.mindarena.submission.repository;

import com.mindarena.submission.model.Submission;
import com.mindarena.submission.model.User;
import com.mindarena.submission.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByVoterAndSubmission(User voter, Submission submission);
}
