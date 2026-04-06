package com.elearning.controller;

import com.elearning.model.entity.User;
import com.elearning.repository.UserRepository;
import com.elearning.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class GlobalNavigationAdvice {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @ModelAttribute
    public void populateNavigation(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return;
        }

        UserDetails userDetails = (UserDetails) principal;
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return;
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        model.addAttribute("unreadNotificationsPreview", notificationService.findUnreadPreview(user.getId(), 5));
    }
}
