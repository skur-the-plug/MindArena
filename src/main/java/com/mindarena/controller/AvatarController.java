package com.mindarena.controller;

import com.mindarena.model.User;
import com.mindarena.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AvatarController {

    private final UserRepository userRepository;

    public AvatarController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping(value = "/users/{id}/avatar.svg", produces = "image/svg+xml")
    public ResponseEntity<byte[]> avatar(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        String initials = initials(user);
        String hue = Integer.toString(Math.abs(user.getEmail().hashCode()) % 360);
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" width="96" height="96" viewBox="0 0 96 96">
                  <defs>
                    <linearGradient id="g" x1="0" x2="1" y1="0" y2="1">
                      <stop offset="0%%" stop-color="hsl(%s 72%% 48%%)"/>
                      <stop offset="100%%" stop-color="hsl(%s 68%% 34%%)"/>
                    </linearGradient>
                  </defs>
                  <rect width="96" height="96" rx="18" fill="url(#g)"/>
                  <text x="50%%" y="54%%" dominant-baseline="middle" text-anchor="middle" font-family="Inter, Arial, sans-serif" font-size="34" font-weight="800" fill="white">%s</text>
                </svg>
                """.formatted(hue, Integer.toString((Integer.parseInt(hue) + 48) % 360), escape(initials));
        return ResponseEntity.ok()
                .header("Content-Type", "image/svg+xml")
                .body(svg.getBytes(StandardCharsets.UTF_8));
    }

    private String initials(User user) {
        String name = user.getFullName() == null || user.getFullName().isBlank() ? user.getEmail() : user.getFullName();
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
