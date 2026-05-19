package com.mindarena.domain.submissions.controller;

import com.mindarena.domain.challenges.model.Challenge;
import com.mindarena.domain.identity.model.User;
import com.mindarena.domain.challenges.service.ChallengeService;
import com.mindarena.domain.identity.service.CurrentUserService;
import com.mindarena.domain.identity.service.PrivilegeService;
import com.mindarena.domain.submissions.service.SubmissionService;
import com.mindarena.domain.submissions.service.SubmissionTemplateService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SubmissionController {

    private final CurrentUserService currentUserService;
    private final ChallengeService challengeService;
    private final SubmissionService submissionService;
    private final PrivilegeService privilegeService;
    private final SubmissionTemplateService submissionTemplateService;

    public SubmissionController(
            CurrentUserService currentUserService,
            ChallengeService challengeService,
            SubmissionService submissionService,
            PrivilegeService privilegeService,
            SubmissionTemplateService submissionTemplateService
    ) {
        this.currentUserService = currentUserService;
        this.challengeService = challengeService;
        this.submissionService = submissionService;
        this.privilegeService = privilegeService;
        this.submissionTemplateService = submissionTemplateService;
    }

    @GetMapping("/submissions/{id}")
    public String submission(
            @PathVariable Long id,
            Authentication authentication,
            Model model
    ) {
        User user = currentUserService.requireUser(authentication);
        var submission = submissionService.requireSubmission(id);
        Challenge challenge = submission.getChallenge();
        var template = submissionTemplateService.requireTemplate(challenge.getArena(), challenge.getTemplateType());
        model.addAttribute("submission", submission);
        model.addAttribute("challenge", challenge);
        model.addAttribute("selectedSubmissionTemplate", template);
        model.addAttribute("submissionContent", submissionTemplateService.contentMap(submission.getContentJson()));
        model.addAttribute("comments", submissionService.commentsFor(submission));
        model.addAttribute("canVote", privilegeService.canVote(user));
        model.addAttribute("canSelectBestAnswer", privilegeService.canSelectBestAnswer(user, challenge));
        model.addAttribute("upvoteXp", submissionService.upvotePoints(challenge));
        model.addAttribute("bestAnswerXp", submissionService.bestAnswerPoints(challenge));
        return "submissions/detail";
    }

    @PostMapping("/challenges/{challengeId}/submissions")
    public String submit(
            @PathVariable Long challengeId,
            @RequestParam(required = false) @Size(max = 12000) String contentJson,
            @RequestParam(required = false) @Size(max = 4000) String content,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        User user = currentUserService.requireUser(authentication);
        Challenge challenge = challengeService.requireChallenge(challengeId);
        try {
            var template = submissionTemplateService.requireTemplate(challenge.getArena(), challenge.getTemplateType());
            String json = contentJson == null || contentJson.isBlank() ? submissionTemplateService.defaultJson(template) : contentJson;
            submissionTemplateService.validateContent(template, json);
            String readable = submissionTemplateService.readableContent(template, json);
            submissionService.submit(user, challenge, readable, json);
            redirectAttributes.addFlashAttribute("message", "Submission posted. +10 XP");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/challenges/" + challengeId;
    }

    @PostMapping("/submissions/{id}/comments")
    public String comment(
            @PathVariable Long id,
            @RequestParam @NotBlank @Size(max = 1000) String content,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        User user = currentUserService.requireUser(authentication);
        var submission = submissionService.requireSubmission(id);
        submissionService.comment(user, submission, content);
        redirectAttributes.addFlashAttribute("message", "Comment posted.");
        return "redirect:/submissions/" + id;
    }

    @PostMapping("/submissions/{id}/upvote")
    public String upvote(
            @PathVariable Long id,
            @RequestParam Long challengeId,
            @RequestParam(defaultValue = "false") boolean returnToSubmission,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        User user = currentUserService.requireUser(authentication);
        if (!privilegeService.canVote(user)) {
            redirectAttributes.addFlashAttribute("error", "Reach Explorer rank to vote on submissions.");
            return "redirect:/challenges/" + challengeId;
        }
        var submission = submissionService.requireSubmission(id);
        boolean voted = submissionService.upvote(user, id);
        redirectAttributes.addFlashAttribute("message", voted ? "Vote counted. +" + submissionService.upvotePoints(submission.getChallenge()) + " XP for the author." : "Vote skipped.");
        if (returnToSubmission) {
            return "redirect:/submissions/" + id;
        }
        return "redirect:/challenges/" + challengeId;
    }

    @PostMapping("/submissions/{id}/best")
    public String selectBest(
            @PathVariable Long id,
            @RequestParam Long challengeId,
            @RequestParam(defaultValue = "false") boolean returnToSubmission,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        User user = currentUserService.requireUser(authentication);
        try {
            submissionService.selectBestAnswer(user, id, privilegeService);
            redirectAttributes.addFlashAttribute("message", "Best answer selected. +25 XP for the author.");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        if (returnToSubmission) {
            return "redirect:/submissions/" + id;
        }
        return "redirect:/challenges/" + challengeId;
    }
}
