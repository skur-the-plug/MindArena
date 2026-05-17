package com.mindarena.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindarena.dto.SubmissionTemplate;
import com.mindarena.model.Arena;
import com.mindarena.model.ArenaType;
import com.mindarena.model.TemplateType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SubmissionTemplateService {

    private final ObjectMapper objectMapper;

    public SubmissionTemplateService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<SubmissionTemplate> templatesFor(Arena arena) {
        return switch (ArenaType.fromArenaName(arena.getName())) {
            case CODING -> List.of(
                    template(TemplateType.CODE_SOLUTION, "Code Solution", "Mini editor for algorithms, SQL, and programming answers.", true, "coding", "code", List.of("language", "codeSolution", "explanation")),
                    template(TemplateType.SYSTEM_DESIGN_FIX, "System Design Fix", "Architecture board for diagnosis, APIs, data, and trade-offs.", false, "coding", "server", List.of("problemDiagnosis", "proposedArchitecture", "technologiesUsed", "tradeoffs"))
            );
            case BUSINESS -> List.of(
                    template(TemplateType.STARTUP_PITCH_CANVAS, "Startup Pitch Canvas", "Business model canvas for startup and product ideas.", false, "business", "rocket", List.of("startupName", "problem", "targetCustomers", "proposedSolution", "businessModel", "uniqueValueProposition")),
                    template(TemplateType.GROWTH_STRATEGY_PLAN, "Growth Strategy Plan", "Strategic dashboard for launch, channels, pricing, and metrics.", false, "business", "chart", List.of("productOrService", "targetMarket", "marketingChannels", "competitorAnalysis", "growthPlan", "successMetrics"))
            );
            case CREATIVITY -> List.of(
                    template(TemplateType.CREATIVE_CONCEPT_BRIEF, "Creative Concept Brief", "Agency-style moodboard for concepts, slogans, and visual direction.", false, "creativity", "spark", List.of("conceptTitle", "mainIdea", "targetAudience", "visualDirection")),
                    template(TemplateType.VISUAL_STORYBOARD, "Visual Storyboard", "Three-panel storyboard for videos, ads, posters, and social concepts.", false, "creativity", "frames", List.of("storyTitle", "scene1", "scene2", "scene3", "moodStyle"))
            );
            case DEBATE -> List.of(
                    template(TemplateType.ARGUMENT_BATTLE, "Argument Battle", "For/Against battle card with claims, arguments, counterargument, and conclusion.", false, "debate", "scale", List.of("position", "mainClaim", "argument1", "argument2", "counterargument", "finalConclusion")),
                    template(TemplateType.CRITICAL_ANALYSIS_CASE, "Critical Analysis Case", "Academic analysis worksheet for reflective and ethical questions.", false, "debate", "essay", List.of("topicInterpretation", "mainProblem", "analysis", "opposingView", "personalPosition", "finalSynthesis"))
            );
        };
    }

    public SubmissionTemplate requireTemplate(Arena arena, String templateId) {
        return templatesFor(arena).stream()
                .filter(template -> template.getId().equals(templateId))
                .findFirst()
                .orElseGet(() -> templatesFor(arena).get(0));
    }

    public SubmissionTemplate requireTemplate(Arena arena, TemplateType templateType) {
        if (templateType == null) {
            return templatesFor(arena).get(0);
        }
        return templatesFor(arena).stream()
                .filter(template -> template.getType() == templateType)
                .findFirst()
                .orElseGet(() -> templatesFor(arena).get(0));
    }

    public void validateContent(SubmissionTemplate template, String contentJson) {
        Map<String, Object> content = parseContent(contentJson);
        for (String field : template.getRequiredFields()) {
            Object value = content.get(field);
            if (value instanceof List<?> values) {
                if (values.isEmpty()) {
                    throw new IllegalArgumentException("Complete all required fields before submitting.");
                }
            } else if (value == null || value.toString().isBlank()) {
                throw new IllegalArgumentException("Complete all required fields before submitting.");
            }
        }
    }

    public String readableContent(SubmissionTemplate template, String contentJson) {
        Map<String, Object> content = contentMap(contentJson);
        Map<String, String> labels = labelsFor(template.getType());
        StringBuilder builder = new StringBuilder(template.getName()).append("\n\n");
        labels.forEach((key, label) -> {
            Object value = content.get(key);
            if (value != null && !value.toString().isBlank() && !(value instanceof List<?> values && values.isEmpty())) {
                builder.append(label).append("\n");
                if (value instanceof List<?> values) {
                    builder.append(String.join(", ", values.stream().map(Object::toString).toList()));
                } else {
                    builder.append(value);
                }
                builder.append("\n\n");
            }
        });
        return builder.toString().trim();
    }

    public String defaultJson(SubmissionTemplate template) {
        Map<String, Object> content = new LinkedHashMap<>();
        labelsFor(template.getType()).keySet().forEach(key -> content.put(key, ""));
        try {
            return objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    public Map<String, Object> contentMap(String contentJson) {
        if (contentJson == null || contentJson.isBlank()) {
            return Map.of();
        }
        return parseContent(contentJson);
    }

    private SubmissionTemplate template(TemplateType type, String name, String description, boolean codeTemplate, String arenaClass, String icon, List<String> requiredFields) {
        return new SubmissionTemplate(type.name(), type, name, description, previewBody(type), codeTemplate, arenaClass, icon, requiredFields);
    }

    private Map<String, Object> parseContent(String contentJson) {
        try {
            return objectMapper.readValue(contentJson, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new IllegalArgumentException("Submission content must be valid JSON.");
        }
    }

    private Map<String, String> labelsFor(TemplateType type) {
        Map<String, String> labels = new LinkedHashMap<>();
        switch (type) {
            case CODE_SOLUTION -> {
                labels.put("language", "Programming Language");
                labels.put("codeSolution", "Code Solution");
                labels.put("explanation", "Explanation of Approach");
                labels.put("timeComplexity", "Time Complexity");
                labels.put("spaceComplexity", "Space Complexity");
                labels.put("testCases", "Test Cases");
            }
            case SYSTEM_DESIGN_FIX -> {
                labels.put("problemDiagnosis", "Problem Diagnosis");
                labels.put("proposedArchitecture", "Proposed Architecture");
                labels.put("technologiesUsed", "Technologies Used");
                labels.put("databaseChanges", "Database Changes");
                labels.put("apiEndpoints", "API Endpoints");
                labels.put("tradeoffs", "Trade-offs");
            }
            case STARTUP_PITCH_CANVAS -> {
                labels.put("startupName", "Startup Name");
                labels.put("problem", "Problem");
                labels.put("targetCustomers", "Target Customers");
                labels.put("proposedSolution", "Proposed Solution");
                labels.put("businessModel", "Business Model");
                labels.put("competitors", "Competitors");
                labels.put("uniqueValueProposition", "Unique Value Proposition");
            }
            case GROWTH_STRATEGY_PLAN -> {
                labels.put("productOrService", "Product or Service");
                labels.put("targetMarket", "Target Market");
                labels.put("marketingChannels", "Marketing Channels");
                labels.put("pricingStrategy", "Pricing Strategy");
                labels.put("competitorAnalysis", "Competitor Analysis");
                labels.put("growthPlan", "Growth Plan");
                labels.put("successMetrics", "Key Success Metrics");
            }
            case CREATIVE_CONCEPT_BRIEF -> {
                labels.put("conceptTitle", "Concept Title");
                labels.put("mainIdea", "Main Idea");
                labels.put("targetAudience", "Target Audience");
                labels.put("inspiration", "Inspiration");
                labels.put("slogan", "Slogan or Key Message");
                labels.put("visualDirection", "Visual Direction");
                labels.put("assetLink", "Image/File/Link");
            }
            case VISUAL_STORYBOARD -> {
                labels.put("storyTitle", "Story Title");
                labels.put("scene1", "Scene 1");
                labels.put("scene2", "Scene 2");
                labels.put("scene3", "Scene 3");
                labels.put("moodStyle", "Mood / Style");
                labels.put("toolsUsed", "Tools Used");
                labels.put("finalVisualLink", "Final Visual Link or File");
            }
            case ARGUMENT_BATTLE -> {
                labels.put("position", "Position");
                labels.put("mainClaim", "Main Claim");
                labels.put("argument1", "Argument 1");
                labels.put("argument2", "Argument 2");
                labels.put("argument3", "Argument 3");
                labels.put("counterargument", "Counterargument");
                labels.put("finalConclusion", "Final Conclusion");
            }
            case CRITICAL_ANALYSIS_CASE -> {
                labels.put("topicInterpretation", "Topic Interpretation");
                labels.put("mainProblem", "Main Problem");
                labels.put("analysis", "Analysis");
                labels.put("realLifeExample", "Real-Life Example");
                labels.put("opposingView", "Opposing View");
                labels.put("personalPosition", "Personal Position");
                labels.put("finalSynthesis", "Final Synthesis");
            }
        }
        return labels;
    }

    private String previewBody(TemplateType type) {
        return switch (type) {
            case CODE_SOLUTION -> "Terminal header, language selector, dark code editor, explanation, complexity, and tests.";
            case SYSTEM_DESIGN_FIX -> "Architecture board with Diagnosis, Architecture, Database, API, and Trade-off blocks.";
            case STARTUP_PITCH_CANVAS -> "Startup canvas grid covering problem, customers, solution, model, competitors, and UVP.";
            case GROWTH_STRATEGY_PLAN -> "Consultant-style growth dashboard with market, channels, pricing, competitors, plan, and metrics.";
            case CREATIVE_CONCEPT_BRIEF -> "Moodboard brief with highlighted concept title, slogan, inspiration, and visual direction.";
            case VISUAL_STORYBOARD -> "Three storyboard panels plus mood, tools, and final visual link.";
            case ARGUMENT_BATTLE -> "For/Against selector, claim, argument cards, counterargument, and closing conclusion.";
            case CRITICAL_ANALYSIS_CASE -> "Numbered academic cards for interpretation, problem, analysis, examples, opposing view, and synthesis.";
        };
    }
}
