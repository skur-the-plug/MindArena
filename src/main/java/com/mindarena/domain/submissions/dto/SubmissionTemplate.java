package com.mindarena.domain.submissions.dto;

import com.mindarena.domain.challenges.model.TemplateType;
import java.util.List;

public class SubmissionTemplate {

    private final String id;
    private final TemplateType type;
    private final String name;
    private final String description;
    private final String body;
    private final boolean codeTemplate;
    private final String arenaClass;
    private final String icon;
    private final List<String> requiredFields;

    public SubmissionTemplate(String id, TemplateType type, String name, String description, String body, boolean codeTemplate, String arenaClass, String icon, List<String> requiredFields) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.body = body;
        this.codeTemplate = codeTemplate;
        this.arenaClass = arenaClass;
        this.icon = icon;
        this.requiredFields = requiredFields;
    }

    public String getId() {
        return id;
    }

    public TemplateType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBody() {
        return body;
    }

    public boolean isCodeTemplate() {
        return codeTemplate;
    }

    public String getArenaClass() {
        return arenaClass;
    }

    public String getIcon() {
        return icon;
    }

    public List<String> getRequiredFields() {
        return requiredFields;
    }
}
