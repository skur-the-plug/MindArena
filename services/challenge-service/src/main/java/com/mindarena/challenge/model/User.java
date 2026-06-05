package com.mindarena.challenge.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    private Long id;

    private String fullName;

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }
}
