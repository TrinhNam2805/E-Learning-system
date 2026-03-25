package com.elearning.controller;

import com.elearning.model.entity.User;
import com.elearning.repository.UserRepository;
import com.elearning.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        model.addAttribute("notifications", notificationService.findByUserId(user.getId()));
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "notification/list";
    }

    @PostMapping("/mark-all-read")
    public String markAllRead(@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes ra) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user != null) {
            notificationService.markAllRead(user.getId());
            ra.addFlashAttribute("success", "All notifications marked as read.");
        }
        return "redirect:/notifications";
    }
}
