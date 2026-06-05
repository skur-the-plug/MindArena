package com.mindarena.submission.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Challenge {
    @Id
    private Long id;
    private String title;
    private String difficulty;
    @ManyToOne(optional = false)
    private Arena arena;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Arena getArena() {
        return arena;
    }
}
