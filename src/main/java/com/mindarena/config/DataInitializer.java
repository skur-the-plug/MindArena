package com.mindarena.config;

import com.mindarena.model.Arena;
import com.mindarena.model.Challenge;
import com.mindarena.model.Role;
import com.mindarena.model.TemplateType;
import com.mindarena.model.User;
import com.mindarena.repository.ArenaRepository;
import com.mindarena.repository.ChallengeRepository;
import com.mindarena.repository.PlatformNewsRepository;
import com.mindarena.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ArenaRepository arenaRepository;
    private final ChallengeRepository challengeRepository;
    private final PlatformNewsRepository platformNewsRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            ArenaRepository arenaRepository,
            ChallengeRepository challengeRepository,
            PlatformNewsRepository platformNewsRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.arenaRepository = arenaRepository;
        this.challengeRepository = challengeRepository;
        this.platformNewsRepository = platformNewsRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedArenas();
        seedChallenges();
        seedPlatformNews();
        backfillChallengeMetadata();
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail("admin@mindarena.local")) {
            return;
        }

        User admin = new User();
        admin.setFullName("MindArena Admin");
        admin.setEmail("admin@mindarena.local");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setSkills("moderation, challenge design, product demos");
        admin.setInterests("learning platforms, competition, youth communities");
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
    }

    private void seedArenas() {
        createArena("Coding Arena", "Solve programming and architecture challenges.", "blue");
        createArena("Business Arena", "Pitch startup ideas and strategy solutions.", "green");
        createArena("Creativity Arena", "Turn prompts into original concepts and campaigns.", "pink");
        createArena("Debate Arena", "Defend opinions with structured arguments.", "orange");
    }

    private void createArena(String name, String description, String color) {
        arenaRepository.findByName(name).orElseGet(() -> {
            Arena arena = new Arena();
            arena.setName(name);
            arena.setDescription(description);
            arena.setColor(color);
            return arenaRepository.save(arena);
        });
    }

    private void seedChallenges() {
        if (challengeRepository.count() > 0) {
            return;
        }

        List<Arena> arenas = arenaRepository.findAll();
        for (Arena arena : arenas) {
            Challenge challenge = new Challenge();
            challenge.setArena(arena);
            challenge.setDeadline(LocalDateTime.now().plusDays(7));
            challenge.setTitle(defaultTitle(arena.getName()));
            challenge.setBrief(defaultBrief(arena.getName()));
            challenge.setTemplateType(defaultTemplateType(arena.getName()));
            challenge.setSubmissionTemplateName(defaultTemplateName(arena.getName()));
            challenge.setSubmissionTemplateBody(defaultTemplateBody(arena.getName()));
            challengeRepository.save(challenge);
        }
    }

    private String defaultTitle(String arenaName) {
        return switch (arenaName) {
            case "Coding Arena" -> "Design a leaderboard API";
            case "Business Arena" -> "Pitch a youth-focused startup";
            case "Creativity Arena" -> "Create a campaign for learning motivation";
            case "Debate Arena" -> "Should schools grade teamwork?";
            default -> "Solve this arena challenge";
        };
    }

    private void backfillChallengeMetadata() {
        User admin = userRepository.findByEmail("admin@mindarena.local").orElse(null);
        for (Challenge challenge : challengeRepository.findAll()) {
            boolean changed = false;
            if (challenge.getCreator() == null && admin != null) {
                challenge.setCreator(admin);
                changed = true;
            }
            if (!challenge.hasSubmissionTemplate()) {
                challenge.setTemplateType(defaultTemplateType(challenge.getArena().getName()));
                challenge.setSubmissionTemplateName(defaultTemplateName(challenge.getArena().getName()));
                challenge.setSubmissionTemplateBody(defaultTemplateBody(challenge.getArena().getName()));
                changed = true;
            }
            if (challenge.getTemplateType() == null) {
                challenge.setTemplateType(defaultTemplateType(challenge.getArena().getName()));
                changed = true;
            }
            if (challenge.getDifficulty() == null || challenge.getDifficulty().isBlank()) {
                challenge.setDifficulty("Intermediate");
                changed = true;
            }
            if (changed) {
                challengeRepository.save(challenge);
            }
        }
    }

    private String defaultBrief(String arenaName) {
        return switch (arenaName) {
            case "Coding Arena" -> "Propose endpoints, tables, and score rules for a reliable leaderboard.";
            case "Business Arena" -> "Describe the target users, core value, revenue model, and first launch plan.";
            case "Creativity Arena" -> "Write a short campaign concept with title, slogan, and execution idea.";
            case "Debate Arena" -> "Give a clear position, two arguments, and one counterargument.";
            default -> "Submit a structured response that other users can evaluate.";
        };
    }

    private void seedPlatformNews() {
        if (platformNewsRepository.count() > 0) {
            return;
        }
        User admin = userRepository.findByEmail("admin@mindarena.local").orElse(null);
        com.mindarena.model.PlatformNews news = new com.mindarena.model.PlatformNews();
        news.setAuthor(admin);
        news.setCategory("Weekly Challenge");
        news.setTitle("Weekly Challenge: Create a startup idea for students");
        news.setBody("A fresh weekly challenge is now live. Join an arena, submit your best answer, and climb the ranks.");
        platformNewsRepository.save(news);
    }

    private String defaultTemplateName(String arenaName) {
        return switch (arenaName) {
            case "Coding Arena" -> "Code Solution";
            case "Business Arena" -> "Startup Pitch Canvas";
            case "Creativity Arena" -> "Creative Concept Brief";
            case "Debate Arena" -> "Argument Battle";
            default -> "Code Solution";
        };
    }

    private TemplateType defaultTemplateType(String arenaName) {
        return switch (arenaName) {
            case "Business Arena" -> TemplateType.STARTUP_PITCH_CANVAS;
            case "Creativity Arena" -> TemplateType.CREATIVE_CONCEPT_BRIEF;
            case "Debate Arena" -> TemplateType.ARGUMENT_BATTLE;
            default -> TemplateType.CODE_SOLUTION;
        };
    }

    private String defaultTemplateBody(String arenaName) {
        return switch (arenaName) {
            case "Coding Arena" -> """
                    Domain model:

                    Endpoints:
                    - METHOD /path:

                    Database tables:

                    Validation and permissions:

                    Scoring/ranking rules:

                    Failure modes:

                    Minimal implementation sketch:
                    """;
            default -> """
                    Position:

                    Key idea:

                    Execution:

                    Why it works:

                    Risks:
                    """;
        };
    }
}
