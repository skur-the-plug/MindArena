package com.mindarena.ranking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;

@Entity
public class Submission implements Serializable {

    @Id
    private Long id;

    @ManyToOne(optional = false)
    private Challenge challenge;

    @ManyToOne(optional = false)
    private User author;

    @Column(nullable = false)
    private int upvotes;

    @Column(nullable = false)
    private boolean bestAnswer;

    public Long getId() {
        return id;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public User getAuthor() {
        return author;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public boolean isBestAnswer() {
        return bestAnswer;
    }
}
