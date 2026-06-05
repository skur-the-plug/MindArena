package com.mindarena.submission.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Arena {
    @Id
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
