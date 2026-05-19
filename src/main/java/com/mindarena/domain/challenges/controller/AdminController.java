package com.mindarena.domain.challenges.controller;

import com.mindarena.domain.challenges.model.Challenge;
import com.mindarena.domain.notifications.model.NotificationType;
import com.mindarena.domain.identity.model.User;
import com.mindarena.domain.identity.repository.UserRepository;
import com.mindarena.domain.challenges.service.ArenaService;
import com.mindarena.domain.challenges.service.ChallengeService;
import com.mindarena.domain.identity.service.CurrentUserService;
import com.mindarena.domain.submissions.service.SubmissionTemplateService;
import com.mindarena.domain.notifications.service.NotificationService;
import java.time.LocalDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final ChallengeService challengeService;
    private final ArenaService arenaService;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final SubmissionTemplateService submissionTemplateService;
    private final NotificationService notificationService;

    public AdminController(
            ChallengeService challengeService,
            ArenaService arenaService,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            SubmissionTemplateService submissionTemplateService,
            NotificationService notificationService
    ) {
        this.challengeService = challengeService;
        this.arenaService = arenaService;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.submissionTemplateService = submissionTemplateService;
        this.notificationService = notificationService;
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("challenge", newChallenge());
        model.addAttribute("activeChallenges", challengeService.activeChallenges());
        model.addAttribute("archivedChallenges", challengeService.archivedChallenges());
        model.addAttribute("arenas", arenaService.findAll());
        model.addAttribute("templateOptionsByArena", arenaService.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        arena -> arena.getId(),
                        submissionTemplateService::templatesFor
                )));
        model.addAttribute("users", userRepository.findAll());
        return "admin/index";
    }

    @PostMapping("/admin/challenges")
    public String createChallenge(
            @RequestParam String title,
            @RequestParam Long arenaId,
            @RequestParam String brief,
            @RequestParam(defaultValue = "Intermediate") String difficulty,
            @RequestParam(required = false) String templateId,
            @RequestParam LocalDateTime deadline,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        User creator = currentUserService.requireUser(authentication);
        var arena = arenaService.requireArena(arenaId);
        var template = submissionTemplateService.requireTemplate(arena, templateId);
        challengeService.createChallenge(creator, arena, title, brief, difficulty, template.getType(), template.getName(), template.getBody(), deadline);
        redirectAttributes.addFlashAttribute("message", "Challenge created. +15 XP");
        return "redirect:/admin";
    }

    @PostMapping("/admin/challenges/{id}/toggle")
    public String toggleChallenge(@PathVariable Long id) {
        Challenge challenge = challengeService.requireChallenge(id);
        challenge.setActive(!challenge.isActive());
        challengeService.save(challenge);
        notificationService.notify(
                challenge.getCreator(),
                NotificationType.MODERATION,
                challenge.isActive()
                        ? "An admin unarchived your challenge: " + challenge.getTitle() + "."
                        : "An admin archived your challenge: " + challenge.getTitle() + ".",
                "/challenges/" + challenge.getId()
        );
        return "redirect:/admin";
    }

    private Challenge newChallenge() {
        Challenge challenge = new Challenge();
        challenge.setDeadline(LocalDateTime.now().plusDays(7));
        return challenge;
    }
}
