package com.elearning.controller;

import com.elearning.repository.UserRepository;
import com.elearning.service.ForumService;
import com.elearning.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AnnouncementController {

    private final ForumService forumService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @GetMapping("/announcements")
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("posts", forumService.findRecentAnnouncements(50));
        if (userDetails != null) {
            userRepository.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                model.addAttribute("currentUser", user);
                model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
            });
        }
        return "announcements/list";
    }
}
