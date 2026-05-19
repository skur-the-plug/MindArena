package com.mindarena.domain.submissions.repository;

import com.mindarena.domain.submissions.model.Submission;
import com.mindarena.domain.identity.model.User;
import com.mindarena.domain.submissions.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByVoterAndSubmission(User voter, Submission submission);
}
