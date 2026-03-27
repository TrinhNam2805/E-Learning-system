package com.elearning.controller;

import com.elearning.model.entity.User;
import com.elearning.repository.UserRepository;
import com.elearning.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @GetMapping
    public String rankings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("rankings", rankingService.getLeaderboard());

        if (userDetails != null) {
            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            if (user != null) {
                model.addAttribute("currentUser", user);
                model.addAttribute("myRank", rankingService.getMyRank(user.getId()));
                model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
            }
        }
        return "ranking/list";
    }
}
