package com.mindarena.controller;

import com.mindarena.model.User;
import com.mindarena.service.CurrentUserService;
import com.mindarena.service.UserService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private static final Path PROFILE_UPLOAD_DIR = Path.of("uploads", "profile-icons");
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    private final CurrentUserService currentUserService;
    private final UserService userService;

    public ProfileController(CurrentUserService currentUserService, UserService userService) {
        this.currentUserService = currentUserService;
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        model.addAttribute("user", currentUserService.requireUser(authentication));
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            Authentication authentication,
            @Valid @ModelAttribute User submitted,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasFieldErrors("fullName") || bindingResult.hasFieldErrors("skills")
                || bindingResult.hasFieldErrors("interests")) {
            return "profile";
        }

        User existing = currentUserService.requireUser(authentication);
        userService.updateProfile(existing, submitted);
        redirectAttributes.addFlashAttribute("message", "Profile updated.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/avatar")
    public String updateAvatar(
            Authentication authentication,
            @RequestParam MultipartFile avatar,
            RedirectAttributes redirectAttributes
    ) throws IOException {
        if (avatar.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Choose an image first.");
            return "redirect:/profile";
        }
        if (!ALLOWED_IMAGE_TYPES.contains(avatar.getContentType())) {
            redirectAttributes.addFlashAttribute("error", "Use a JPG, PNG, GIF, or WEBP image.");
            return "redirect:/profile";
        }

        Files.createDirectories(PROFILE_UPLOAD_DIR);
        String extension = extensionFor(avatar.getContentType());
        String fileName = UUID.randomUUID() + extension;
        Path destination = PROFILE_UPLOAD_DIR.resolve(fileName).normalize();
        avatar.transferTo(destination.toAbsolutePath());

        User user = currentUserService.requireUser(authentication);
        userService.updateProfileImage(user, "/uploads/profile-icons/" + fileName);
        redirectAttributes.addFlashAttribute("message", "Profile image updated.");
        return "redirect:/profile";
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
