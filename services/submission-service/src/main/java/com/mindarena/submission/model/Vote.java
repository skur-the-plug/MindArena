package com.mindarena.submission.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"voter_id", "submission_id"}))
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private User voter;
    @ManyToOne(optional = false)
    private Submission submission;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Vote() {}
    public Vote(User voter, Submission submission) {
        this.voter = voter;
        this.submission = submission;
    }
}
