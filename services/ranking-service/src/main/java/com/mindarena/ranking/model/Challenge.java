package com.mindarena.ranking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;

@Entity
public class Challenge implements Serializable {

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
