package com.mindarena.repository;

import com.mindarena.model.Submission;
import com.mindarena.model.User;
import com.mindarena.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByVoterAndSubmission(User voter, Submission submission);
}
