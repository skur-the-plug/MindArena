package com.mindarena.domain.challenges.controller;

import com.mindarena.domain.identity.model.User;
import com.mindarena.domain.identity.service.CurrentUserService;
import com.mindarena.domain.notifications.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class NewsController {

    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public NewsController(CurrentUserService currentUserService, NotificationService notificationService) {
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    @GetMapping("/news")
    public String news(Authentication authentication, Model model) {
        User user = currentUserService.requireUser(authentication);
        model.addAttribute("notifications", notificationService.personalNotifications(user));
        model.addAttribute("platformNews", notificationService.platformNews());
        model.addAttribute("unreadCount", notificationService.unreadCount(user));
        return "news/index";
    }

    @PostMapping("/news/notifications/clear")
    public String clearNotifications(Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = currentUserService.requireUser(authentication);
        notificationService.clearNotifications(user);
        redirectAttributes.addFlashAttribute("message", "Notifications cleared.");
        return "redirect:/news";
    }

    @PostMapping("/notifications/{id}/open")
    public String openNotification(@PathVariable Long id, Authentication authentication) {
        User user = currentUserService.requireUser(authentication);
        return "redirect:" + notificationService.markReadAndResolveLink(user, id);
    }

    @PostMapping("/admin/news/clear")
    public String clearPlatformNews(RedirectAttributes redirectAttributes) {
        notificationService.clearPlatformNews();
        redirectAttributes.addFlashAttribute("message", "Platform news cleared.");
        return "redirect:/news";
    }

    @PostMapping("/admin/news")
    public String publishNews(
            @RequestParam String title,
            @RequestParam String body,
            @RequestParam(defaultValue = "Announcement") String category,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        User user = currentUserService.requireUser(authentication);
        notificationService.publishNews(user, title, body, category);
        redirectAttributes.addFlashAttribute("message", "Platform news published.");
        return "redirect:/admin";
    }
}
