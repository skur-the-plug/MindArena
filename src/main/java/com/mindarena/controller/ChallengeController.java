package com.mindarena.controller;

import com.mindarena.model.Challenge;
import com.mindarena.model.User;
import com.mindarena.repository.UserRepository;
import com.mindarena.service.ChallengeJudgeService;
import com.mindarena.service.ChallengeService;
import com.mindarena.service.CurrentUserService;
import com.mindarena.service.PrivilegeService;
import com.mindarena.service.SubmissionService;
import com.mindarena.service.SubmissionTemplateService;
import java.time.LocalDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ChallengeController {

    private final ChallengeService challengeService;
    private final SubmissionService submissionService;
    private final CurrentUserService currentUserService;
    private final PrivilegeService privilegeService;
    private final ChallengeJudgeService challengeJudgeService;
    private final SubmissionTemplateService submissionTemplateService;
    private final UserRepository userRepository;

    public ChallengeController(
            ChallengeService challengeService,
            SubmissionService submissionService,
            CurrentUserService currentUserService,
            PrivilegeService privilegeService,
            ChallengeJudgeService challengeJudgeService,
            SubmissionTemplateService submissionTemplateService,
            UserRepository userRepository
    ) {
        this.challengeService = challengeService;
        this.submissionService = submissionService;
        this.currentUserService = currentUserService;
        this.privilegeService = privilegeService;
        this.challengeJudgeService = challengeJudgeService;
        this.submissionTemplateService = submissionTemplateService;
        this.userRepository = userRepository;
    }

    @GetMapping("/challenges/{id}")
    public String challenge(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "votes") String submissionSort,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "All") String submissionFilter,
            Authentication authentication,
            Model model
    ) {
        Challenge challenge = challengeService.requireChallenge(id);
        User user = currentUserService.requireUser(authentication);
        model.addAttribute("challenge", challenge);
        model.addAttribute("submissions", submissionService.forChallenge(challenge, submissionSort, submissionFilter));
        model.addAttribute("submissionSort", submissionSort);
        model.addAttribute("submissionFilter", submissionFilter);
        model.addAttribute("codeChallenge", isCodeChallenge(challenge));
        model.addAttribute("canVote", privilegeService.canVote(user));
        model.addAttribute("canManageChallenge", privilegeService.canManageChallenge(user, challenge));
        model.addAttribute("canSelectBestAnswer", privilegeService.canSelectBestAnswer(user, challenge));
        model.addAttribute("submissionTemplates", submissionTemplateService.templatesFor(challenge.getArena()));
        model.addAttribute("selectedSubmissionTemplate", submissionTemplateService.requireTemplate(challenge.getArena(), challenge.getTemplateType()));
        model.addAttribute("eligibleJudges", challengeJudgeService.eligibleJudgesFor(challenge));
        model.addAttribute("challengeJudges", challengeJudgeService.judgesFor(challenge));
        return "challenges/detail";
    }

    @PostMapping("/challenges/{id}/toggle-own")
    public String toggleOwnChallenge(@PathVariable Long id, Authentication authentication, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Challenge challenge = challengeService.requireChallenge(id);
        User user = currentUserService.requireUser(authentication);
        if (!privilegeService.canManageChallenge(user, challenge)) {
            redirectAttributes.addFlashAttribute("error", "You can only manage challenges you created.");
            return "redirect:/challenges/" + id;
        }
        challenge.setActive(!challenge.isActive());
        challengeService.save(challenge);
        redirectAttributes.addFlashAttribute("message", challenge.isActive() ? "Challenge unarchived." : "Challenge archived.");
        return "redirect:/arenas/" + challenge.getArena().getId();
    }

    @PostMapping("/challenges/{id}/update")
    public String updateChallenge(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam String title,
            @org.springframework.web.bind.annotation.RequestParam String brief,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "Intermediate") String difficulty,
            @org.springframework.web.bind.annotation.RequestParam String templateId,
            @org.springframework.web.bind.annotation.RequestParam LocalDateTime deadline,
            Authentication authentication,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes
    ) {
        Challenge challenge = challengeService.requireChallenge(id);
        User user = currentUserService.requireUser(authentication);
        if (!privilegeService.canManageChallenge(user, challenge)) {
            redirectAttributes.addFlashAttribute("error", "You can only edit challenges you created.");
            return "redirect:/challenges/" + id;
        }
        var template = submissionTemplateService.requireTemplate(challenge.getArena(), templateId);
        challengeService.updateChallenge(challenge, title, brief, difficulty, template.getType(), template.getName(), template.getBody(), deadline);
        redirectAttributes.addFlashAttribute("message", "Challenge updated.");
        return "redirect:/challenges/" + id;
    }

    @PostMapping("/challenges/{id}/judges")
    public String addJudge(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam Long judgeId,
            Authentication authentication,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes
    ) {
        Challenge challenge = challengeService.requireChallenge(id);
        User user = currentUserService.requireUser(authentication);
        if (!privilegeService.canManageChallenge(user, challenge)) {
            redirectAttributes.addFlashAttribute("error", "You can only assign judges for challenges you created.");
            return "redirect:/challenges/" + id;
        }
        User judge = userRepository.findById(judgeId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (challenge.getCreator() != null && challenge.getCreator().getId().equals(judge.getId())) {
            redirectAttributes.addFlashAttribute("error", "The challenge creator is already the default judge.");
            return "redirect:/challenges/" + id;
        }
        if (!privilegeService.canVote(judge)) {
            redirectAttributes.addFlashAttribute("error", "Only Explorer rank or higher can be a judge.");
            return "redirect:/challenges/" + id;
        }
        challengeJudgeService.addJudge(challenge, judge);
        redirectAttributes.addFlashAttribute("message", judge.getFullName() + " can now select the best answer.");
        return "redirect:/challenges/" + id;
    }

    @PostMapping("/challenges/{id}/judges/remove")
    public String removeJudge(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam Long judgeId,
            Authentication authentication,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes
    ) {
        Challenge challenge = challengeService.requireChallenge(id);
        User user = currentUserService.requireUser(authentication);
        if (!privilegeService.canManageChallenge(user, challenge)) {
            redirectAttributes.addFlashAttribute("error", "You can only assign judges for challenges you created.");
            return "redirect:/challenges/" + id;
        }
        User judge = userRepository.findById(judgeId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        challengeJudgeService.removeJudge(challenge, judge);
        redirectAttributes.addFlashAttribute("message", "Judge removed.");
        return "redirect:/challenges/" + id;
    }

    private boolean isCodeChallenge(Challenge challenge) {
        return "Coding Arena".equalsIgnoreCase(challenge.getArena().getName())
                || (challenge.getSubmissionTemplateBody() != null && challenge.getSubmissionTemplateBody().contains("```"));
    }
}
