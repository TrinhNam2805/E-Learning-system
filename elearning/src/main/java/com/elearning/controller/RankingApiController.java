package com.elearning.controller;

import com.elearning.model.dto.ranking.BadgeCollectionDto;
import com.elearning.model.dto.ranking.LeaderboardEntryDto;
import com.elearning.model.entity.User;
import com.elearning.repository.UserRepository;
import com.elearning.service.BadgeService;
import com.elearning.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingApiController {

    private final RankingService rankingService;
    private final BadgeService badgeService;
    private final UserRepository userRepository;

    @GetMapping("/leaderboard")
    public List<LeaderboardEntryDto> leaderboard() {
        return rankingService.getLeaderboard();
    }

    @GetMapping("/me")
    public LeaderboardEntryDto currentUserRank(@AuthenticationPrincipal UserDetails userDetails) {
        User user = requireCurrentStudent(userDetails);
        return rankingService.getMyRank(user.getId());
    }

    @GetMapping("/badges/me")
    public BadgeCollectionDto currentUserBadges(@AuthenticationPrincipal UserDetails userDetails) {
        User user = requireCurrentStudent(userDetails);
        return badgeService.getEarnedBadges(user.getId());
    }

    private User requireCurrentStudent(UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in.");
        }
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found.");
        }
        if (user.getRole() != User.Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can view personal ranking data.");
        }
        return user;
    }
}
