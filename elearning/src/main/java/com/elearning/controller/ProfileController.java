package com.elearning.controller;

import com.elearning.model.entity.User;
import com.elearning.repository.UserRepository;
import com.elearning.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "profile/view";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam(required = false) String phone,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes ra) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        user.setFullName(fullName);
        user.setPhone(phone);
        userService.save(user);
        ra.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/profile";
    }

    @GetMapping("/change-password")
    public String changePasswordPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", user != null ? notificationService.countUnread(user.getId()) : 0);
        return "profile/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes ra) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        if (!userService.checkPassword(user, currentPassword)) {
            ra.addFlashAttribute("error", "Current password is incorrect.");
            return "redirect:/change-password";
        }
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/change-password";
        }
        if (newPassword.length() < 6) {
            ra.addFlashAttribute("error", "New password must be at least 6 characters.");
            return "redirect:/change-password";
        }
        userService.changePassword(user.getId(), newPassword);
        ra.addFlashAttribute("success", "Password changed successfully!");
        return "redirect:/profile";
    }
}
