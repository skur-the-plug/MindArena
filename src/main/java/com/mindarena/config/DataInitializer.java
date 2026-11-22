package com.mindarena.config;

import com.mindarena.domain.challenges.model.Arena;
import com.mindarena.domain.challenges.model.ArenaMembership;
import com.mindarena.domain.challenges.model.Challenge;
import com.mindarena.domain.identity.model.Role;
import com.mindarena.domain.challenges.model.TemplateType;
import com.mindarena.domain.identity.model.User;
import com.mindarena.domain.challenges.repository.ArenaMembershipRepository;
import com.mindarena.domain.challenges.repository.ArenaRepository;
import com.mindarena.domain.challenges.repository.ChallengeRepository;
import com.mindarena.domain.challenges.repository.PlatformNewsRepository;
import com.mindarena.domain.identity.repository.UserRepository;
import com.mindarena.domain.submissions.model.Submission;
import com.mindarena.domain.submissions.model.SubmissionComment;
import com.mindarena.domain.submissions.model.Vote;
import com.mindarena.domain.submissions.repository.SubmissionCommentRepository;
import com.mindarena.domain.submissions.repository.SubmissionRepository;
import com.mindarena.domain.submissions.repository.VoteRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ArenaRepository arenaRepository;
    private final ArenaMembershipRepository arenaMembershipRepository;
    private final ChallengeRepository challengeRepository;
    private final PlatformNewsRepository platformNewsRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionCommentRepository submissionCommentRepository;
    private final VoteRepository voteRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean demoAdminEnabled;
    private final String demoAdminEmail;
    private final String demoAdminPassword;
    private final boolean demoDataEnabled;
    private final String demoUserPassword;

    public DataInitializer(
            ArenaRepository arenaRepository,
            ArenaMembershipRepository arenaMembershipRepository,
            ChallengeRepository challengeRepository,
            PlatformNewsRepository platformNewsRepository,
            UserRepository userRepository,
            SubmissionRepository submissionRepository,
            SubmissionCommentRepository submissionCommentRepository,
            VoteRepository voteRepository,
            PasswordEncoder passwordEncoder,
            @Value("${mindarena.demo-admin.enabled:false}") boolean demoAdminEnabled,
            @Value("${mindarena.demo-admin.email:admin@mindarena.local}") String demoAdminEmail,
            @Value("${mindarena.demo-admin.password:}") String demoAdminPassword,
            @Value("${mindarena.demo-data.enabled:false}") boolean demoDataEnabled,
            @Value("${mindarena.demo-data.user-password:}") String demoUserPassword
    ) {
        this.arenaRepository = arenaRepository;
        this.arenaMembershipRepository = arenaMembershipRepository;
        this.challengeRepository = challengeRepository;
        this.platformNewsRepository = platformNewsRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.submissionCommentRepository = submissionCommentRepository;
        this.voteRepository = voteRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoAdminEnabled = demoAdminEnabled;
        this.demoAdminEmail = demoAdminEmail;
        this.demoAdminPassword = demoAdminPassword;
        this.demoDataEnabled = demoDataEnabled;
        this.demoUserPassword = demoUserPassword;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedArenas();
        seedChallenges();
        seedPlatformNews();
        backfillChallengeMetadata();
        if (demoDataEnabled) {
            List<User> demoUsers = seedDemoUsers();
            seedMemberships(demoUsers);
            seedDemoChallenges();
            seedDemoActivity(demoUsers);
        }
    }

    private void seedAdmin() {
        if (!demoAdminEnabled) {
            return;
        }
        if (demoAdminPassword == null || demoAdminPassword.isBlank()) {
            throw new IllegalStateException("mindarena.demo-admin.password must be set when demo admin seeding is enabled");
        }
        if (userRepository.existsByEmail(demoAdminEmail)) {
            return;
        }

        User admin = new User();
        admin.setFullName("MindArena Admin");
        admin.setEmail(demoAdminEmail);
        admin.setPassword(passwordEncoder.encode(demoAdminPassword));
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

    private List<User> seedDemoUsers() {
        if (demoUserPassword == null || demoUserPassword.isBlank()) {
            throw new IllegalStateException(
                    "mindarena.demo-data.user-password must be set when demo data seeding is enabled"
            );
        }
        return List.of(
                createUser("Nadia Bennani", "nadia@mindarena.local", "data analysis, Python, dashboards",
                        "climate tech, civic data, hackathons", 1580),
                createUser("Omar Haddad", "omar@mindarena.local", "Java, Spring Boot, API design",
                        "backend systems, football analytics, debate", 820),
                createUser("Leila Mansouri", "leila@mindarena.local", "brand strategy, copywriting, visual concepts",
                        "creative campaigns, social impact, design", 430),
                createUser("Youssef El Amrani", "youssef@mindarena.local", "startup research, finance, go-to-market",
                        "student products, marketplaces, pitch decks", 180),
                createUser("Sara Idrissi", "sara@mindarena.local", "argument mapping, public speaking, writing",
                        "ethics, education policy, model UN", 40)
        );
    }

    private User createUser(String fullName, String email, String skills, String interests, int score) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(demoUserPassword));
            user.setSkills(skills);
            user.setInterests(interests);
            user.setRole(Role.USER);
            user.addScore(score);
            return userRepository.save(user);
        });
    }

    private void seedMemberships(List<User> demoUsers) {
        List<Arena> arenas = arenaRepository.findAll();
        for (User user : demoUsers) {
            for (Arena arena : arenas) {
                if (!arenaMembershipRepository.existsByUserAndArena(user, arena)) {
                    arenaMembershipRepository.save(new ArenaMembership(user, arena));
                }
            }
        }
    }

    private void seedChallenges() {
        List<Arena> arenas = arenaRepository.findAll();
        for (Arena arena : arenas) {
            createChallenge(arena, defaultTitle(arena.getName()), defaultBrief(arena.getName()),
                    "Intermediate", LocalDateTime.now().plusDays(7));
        }
    }

    private void seedDemoChallenges() {
        List<Arena> arenas = arenaRepository.findAll();
        for (Arena arena : arenas) {
            switch (arena.getName()) {
                case "Coding Arena" -> {
                    createChallenge(arena, "Optimize an arena feed", "Design a feed ranking approach that balances freshness, quality, and arena relevance.", "Advanced", LocalDateTime.now().plusDays(12));
                    createChallenge(arena, "Debug a slow submission endpoint", "Explain how you would profile, index, cache, and monitor a submission API that slows down under voting traffic.", "Expert", LocalDateTime.now().plusDays(18));
                }
                case "Business Arena" -> createChallenge(arena, "Launch a campus study marketplace", "Create a focused launch plan for a student-to-student tutoring and study resources marketplace.", "Advanced", LocalDateTime.now().plusDays(10));
                case "Creativity Arena" -> createChallenge(arena, "Rebrand the weekly leaderboard", "Create a punchy concept that makes weekly rankings feel aspirational without discouraging beginners.", "Beginner", LocalDateTime.now().plusDays(9));
                case "Debate Arena" -> createChallenge(arena, "Should AI tutors be allowed in exams?", "Take a clear position and handle fairness, access, and learning-quality objections.", "Advanced", LocalDateTime.now().plusDays(14));
                default -> {
                }
            }
        }
    }

    private void createChallenge(Arena arena, String title, String brief, String difficulty, LocalDateTime deadline) {
        boolean exists = challengeRepository.findAll().stream()
                .anyMatch(challenge -> challenge.getTitle().equalsIgnoreCase(title));
        if (exists) {
            return;
        }
        Challenge challenge = new Challenge();
        challenge.setArena(arena);
        challenge.setDeadline(deadline);
        challenge.setTitle(title);
        challenge.setBrief(brief);
        challenge.setDifficulty(difficulty);
        challenge.setTemplateType(defaultTemplateType(arena.getName()));
        challenge.setSubmissionTemplateName(defaultTemplateName(arena.getName()));
        challenge.setSubmissionTemplateBody(defaultTemplateBody(arena.getName()));
        challengeRepository.save(challenge);
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
        User admin = userRepository.findByEmail(demoAdminEmail).orElse(null);
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
        User admin = userRepository.findByEmail(demoAdminEmail).orElse(null);
        com.mindarena.domain.challenges.model.PlatformNews news = new com.mindarena.domain.challenges.model.PlatformNews();
        news.setAuthor(admin);
        news.setCategory("Weekly Challenge");
        news.setTitle("Weekly Challenge: Create a startup idea for students");
        news.setBody("A fresh weekly challenge is now live. Join an arena, submit your best answer, and climb the ranks.");
        platformNewsRepository.save(news);
    }

    private void seedDemoActivity(List<User> demoUsers) {
        if (demoUsers.isEmpty() || submissionRepository.count() > 0) {
            return;
        }

        Map<String, User> users = demoUsers.stream()
                .collect(java.util.stream.Collectors.toMap(User::getEmail, user -> user));
        List<Challenge> challenges = challengeRepository.findAll();
        List<Submission> submissions = new ArrayList<>();

        for (Challenge challenge : challenges) {
            submissions.add(createSubmission(challenge, users.get("nadia@mindarena.local"), answerFor(challenge, "Nadia"), true));
            submissions.add(createSubmission(challenge, users.get("omar@mindarena.local"), answerFor(challenge, "Omar"), challenge.getDifficulty().equals("Expert")));
            if (!"Expert".equals(challenge.getDifficulty())) {
                submissions.add(createSubmission(challenge, users.get("leila@mindarena.local"), answerFor(challenge, "Leila"), false));
            }
            if ("Beginner".equals(challenge.getDifficulty()) || "Advanced".equals(challenge.getDifficulty())) {
                submissions.add(createSubmission(challenge, users.get("youssef@mindarena.local"), answerFor(challenge, "Youssef"), false));
            }
        }

        createVotesAndComments(submissions, demoUsers);
    }

    private Submission createSubmission(Challenge challenge, User author, String content, boolean bestAnswer) {
        Submission submission = new Submission();
        submission.setChallenge(challenge);
        submission.setAuthor(author);
        submission.setContent(content);
        submission.setBestAnswer(bestAnswer);
        return submissionRepository.save(submission);
    }

    private void createVotesAndComments(List<Submission> submissions, List<User> voters) {
        for (Submission submission : submissions) {
            int votesToCreate = submission.isBestAnswer() ? 4 : Math.min(3, Math.max(1, submission.getAuthor().getScore() / 250));
            for (User voter : voters) {
                if (votesToCreate == 0) {
                    break;
                }
                if (!voter.getId().equals(submission.getAuthor().getId())
                        && !voteRepository.existsByVoterAndSubmission(voter, submission)) {
                    submission.addUpvote();
                    voteRepository.save(new Vote(voter, submission));
                    votesToCreate--;
                }
            }
            submissionRepository.save(submission);

            SubmissionComment comment = new SubmissionComment();
            comment.setSubmission(submission);
            comment.setAuthor(voters.stream()
                    .filter(user -> !user.getId().equals(submission.getAuthor().getId()))
                    .findFirst()
                    .orElse(submission.getAuthor()));
            comment.setContent(submission.isBestAnswer()
                    ? "Strong structure and clear tradeoffs. This is a useful benchmark answer."
                    : "Good direction. The next step would be adding sharper success metrics.");
            submissionCommentRepository.save(comment);
        }
    }

    private String answerFor(Challenge challenge, String author) {
        String arena = challenge.getArena().getName();
        return switch (arena) {
            case "Coding Arena" -> author + """
                    's solution:

                    Domain model: users, arenas, challenges, submissions, votes, leaderboard snapshots.

                    Endpoints:
                    - GET /api/leaderboards/global
                    - GET /api/leaderboards/arenas/{arenaId}
                    - POST /api/submissions/{id}/votes

                    Ranking: base submission points plus difficulty-weighted votes and best-answer bonuses.
                    Reliability: unique vote constraint, cached leaderboard reads, async projection rebuilds, and actuator metrics.
                    """;
            case "Business Arena" -> author + """
                    's pitch:

                    Target users: students who need fast, trusted study support before exams.
                    Value: verified peer tutors, small-session booking, and reusable notes bundles.
                    Launch: start with two campuses, ambassador-led onboarding, and weekly challenge rewards.
                    Revenue: platform fee on paid sessions plus featured expert workshops.
                    """;
            case "Creativity Arena" -> author + """
                    's concept:

                    Title: Climb With Proof
                    Slogan: Show the work. Earn the rank.
                    Execution: weekly cards that highlight one useful lesson from the top answer, not just the winner.
                    Why it works: beginners see a path forward while advanced users still compete for status.
                    """;
            case "Debate Arena" -> author + """
                    's position:

                    Position: AI tutors should be allowed for preparation, not live closed-book exams.
                    Argument 1: access improves when students can practice with instant feedback.
                    Argument 2: exams should assess independent mastery unless assistance is explicitly part of the task.
                    Counterargument: banning tools entirely ignores how modern work happens.
                    """;
            default -> author + " submitted a structured response with assumptions, plan, risks, and next steps.";
        };
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
