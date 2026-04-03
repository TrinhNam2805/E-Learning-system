package com.elearning.controller;

import com.elearning.model.dto.ranking.BadgeCollectionDto;
import com.elearning.model.dto.ranking.LeaderboardEntryDto;
import com.elearning.model.entity.User;
import com.elearning.repository.UserRepository;
import com.elearning.service.BadgeService;
import com.elearning.service.NotificationService;
import com.elearning.service.RankingService;
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
    private final BadgeService badgeService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @GetMapping
    public String rankings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        java.util.List<LeaderboardEntryDto> leaderboard = rankingService.getLeaderboard();
        model.addAttribute("rankings", leaderboard);
        model.addAttribute("leaderboardEmpty", leaderboard.isEmpty());

        if (userDetails != null) {
            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            if (user != null) {
                model.addAttribute("currentUser", user);
                model.addAttribute("myRank", rankingService.getMyRank(user.getId()));
                BadgeCollectionDto badges = badgeService.getEarnedBadges(user.getId());
                model.addAttribute("earnedBadges", badges.getBadges());
                model.addAttribute("earnedBadgeCount", badges.getEarnedBadgeCount());
                model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
            }
        }
        return "ranking/list";
    }
}
