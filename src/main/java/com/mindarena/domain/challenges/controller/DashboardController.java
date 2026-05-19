package com.mindarena.domain.challenges.controller;

import com.mindarena.domain.identity.model.Role;
import com.mindarena.domain.identity.model.User;
import com.mindarena.domain.identity.repository.UserRepository;
import com.mindarena.domain.challenges.service.ArenaService;
import com.mindarena.domain.challenges.service.ChallengeService;
import com.mindarena.domain.identity.service.CurrentUserService;
import com.mindarena.domain.rankings.service.RankService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {

    private final CurrentUserService currentUserService;
    private final ArenaService arenaService;
    private final ChallengeService challengeService;
    private final UserRepository userRepository;
    private final RankService rankService;

    public DashboardController(
            CurrentUserService currentUserService,
            ArenaService arenaService,
            ChallengeService challengeService,
            UserRepository userRepository,
            RankService rankService
    ) {
        this.currentUserService = currentUserService;
        this.arenaService = arenaService;
        this.challengeService = challengeService;
        this.userRepository = userRepository;
        this.rankService = rankService;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "deadline") String challengeSort,
            @RequestParam(defaultValue = "All") String difficulty,
            Authentication authentication,
            Model model
    ) {
        User user = currentUserService.requireUser(authentication);
        model.addAttribute("user", user);
        model.addAttribute("memberships", arenaService.memberships(user));
        model.addAttribute("challenges", challengeService.activeChallenges(challengeSort, difficulty));
        model.addAttribute("challengeSort", challengeSort);
        model.addAttribute("difficulty", difficulty);
        model.addAttribute("leaders", userRepository.findTop10ByRoleOrderByScoreDesc(Role.USER));
        model.addAttribute("rank", rankService.rankForScore(user.getScore()));
        return "dashboard";
    }
}
