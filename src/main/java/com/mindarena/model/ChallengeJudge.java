package com.mindarena.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"challenge_id", "judge_id"}))
public class ChallengeJudge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Challenge challenge;

    @ManyToOne(optional = false)
    private User judge;

    public ChallengeJudge() {
    }

    public ChallengeJudge(Challenge challenge, User judge) {
        this.challenge = challenge;
        this.judge = judge;
    }

    public Long getId() {
        return id;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public User getJudge() {
        return judge;
    }
}
