package com.mindarena.domain.challenges.model;

import com.mindarena.domain.identity.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    @Size(max = 1200)
    @Column(length = 1200)
    private String brief;

    @NotNull
    @ManyToOne(optional = false)
    private Arena arena;

    @Size(max = 30)
    private String difficulty = "Intermediate";

    @ManyToOne
    private User creator;

    @Size(max = 80)
    private String submissionTemplateName;

    @Enumerated(EnumType.STRING)
    @Column(length = 60)
    private TemplateType templateType;

    @Size(max = 4000)
    @Column(length = 4000)
    private String submissionTemplateBody;

    private LocalDateTime deadline;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBrief() {
        return brief;
    }

    public void setBrief(String brief) {
        this.brief = brief;
    }

    public Arena getArena() {
        return arena;
    }

    public void setArena(Arena arena) {
        this.arena = arena;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getSubmissionTemplateName() {
        return submissionTemplateName;
    }

    public void setSubmissionTemplateName(String submissionTemplateName) {
        this.submissionTemplateName = submissionTemplateName;
    }

    public TemplateType getTemplateType() {
        return templateType;
    }

    public void setTemplateType(TemplateType templateType) {
        this.templateType = templateType;
    }

    public String getSubmissionTemplateBody() {
        return submissionTemplateBody;
    }

    public void setSubmissionTemplateBody(String submissionTemplateBody) {
        this.submissionTemplateBody = submissionTemplateBody;
    }

    public boolean hasSubmissionTemplate() {
        return templateType != null || (submissionTemplateBody != null && !submissionTemplateBody.isBlank());
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
