package com.mindarena.controller;

import com.mindarena.model.Arena;
import com.mindarena.model.User;
import com.mindarena.service.ArenaService;
import com.mindarena.service.ChallengeService;
import com.mindarena.service.CurrentUserService;
import com.mindarena.service.PrivilegeService;
import com.mindarena.service.SubmissionTemplateService;
import java.time.LocalDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ArenaController {

    private final ArenaService arenaService;
    private final ChallengeService challengeService;
    private final CurrentUserService currentUserService;
    private final PrivilegeService privilegeService;
    private final SubmissionTemplateService submissionTemplateService;

    public ArenaController(
            ArenaService arenaService,
            ChallengeService challengeService,
            CurrentUserService currentUserService,
            PrivilegeService privilegeService,
            SubmissionTemplateService submissionTemplateService
    ) {
        this.arenaService = arenaService;
        this.challengeService = challengeService;
        this.currentUserService = currentUserService;
        this.privilegeService = privilegeService;
        this.submissionTemplateService = submissionTemplateService;
    }

    @GetMapping("/arenas")
    public String arenas(Model model) {
        model.addAttribute("arenas", arenaService.findAll());
        return "arenas/list";
    }

    @PostMapping("/arenas/{id}/join")
    public String join(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = currentUserService.requireUser(authentication);
        arenaService.join(user, id);
        redirectAttributes.addFlashAttribute("message", "Arena joined.");
        return "redirect:/arenas/" + id;
    }

    @GetMapping("/arenas/{id}")
    public String arena(@PathVariable Long id, Authentication authentication, Model model) {
        return arena(id, "deadline", "All", authentication, model);
    }

    @GetMapping(value = "/arenas/{id}", params = {"challengeSort"})
    public String arena(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "deadline") String challengeSort,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "All") String difficulty,
            Authentication authentication,
            Model model
    ) {
        Arena arena = arenaService.requireArena(id);
        User user = currentUserService.requireUser(authentication);
        model.addAttribute("arena", arena);
        model.addAttribute("challenges", challengeService.activeChallengesFor(arena, challengeSort, difficulty));
        model.addAttribute("challengeSort", challengeSort);
        model.addAttribute("difficulty", difficulty);
        model.addAttribute("canCreateChallenge", privilegeService.canCreateChallenge(user));
        model.addAttribute("submissionTemplates", submissionTemplateService.templatesFor(arena));
        return "arenas/detail";
    }

    @PostMapping("/arenas/{id}/challenges")
    public String createChallenge(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam String title,
            @org.springframework.web.bind.annotation.RequestParam String brief,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "Intermediate") String difficulty,
            @org.springframework.web.bind.annotation.RequestParam String templateId,
            @org.springframework.web.bind.annotation.RequestParam LocalDateTime deadline,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        User user = currentUserService.requireUser(authentication);
        if (!privilegeService.canCreateChallenge(user)) {
            redirectAttributes.addFlashAttribute("error", "Reach Challenger rank to create arena challenges.");
            return "redirect:/arenas/" + id;
        }
        Arena arena = arenaService.requireArena(id);
        var template = submissionTemplateService.requireTemplate(arena, templateId);
        challengeService.createChallenge(user, arena, title, brief, difficulty, template.getType(), template.getName(), template.getBody(), deadline);
        redirectAttributes.addFlashAttribute("message", "Challenge created. +15 XP");
        return "redirect:/arenas/" + id;
    }
}
