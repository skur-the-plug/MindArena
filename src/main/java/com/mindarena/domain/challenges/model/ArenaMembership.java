package com.mindarena.domain.challenges.model;

import com.mindarena.domain.identity.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "arena_id"}))
public class ArenaMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Arena arena;

    private LocalDateTime joinedAt = LocalDateTime.now();

    public ArenaMembership() {
    }

    public ArenaMembership(User user, Arena arena) {
        this.user = user;
        this.arena = arena;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Arena getArena() {
        return arena;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
