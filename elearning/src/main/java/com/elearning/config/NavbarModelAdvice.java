package com.elearning.config;

import com.elearning.model.entity.User;
import com.elearning.repository.UserRepository;
import com.elearning.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;

@ControllerAdvice
@RequiredArgsConstructor
public class NavbarModelAdvice {

    private static final int NAVBAR_NOTIF_LIMIT = 10;

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @ModelAttribute
    public void addNavbarNotifications(Authentication authentication, Model model) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            model.addAttribute("navbarNotifications", Collections.emptyList());
            return;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            model.addAttribute("navbarNotifications", Collections.emptyList());
            return;
        }
        String email = ((UserDetails) principal).getUsername();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            model.addAttribute("navbarNotifications", Collections.emptyList());
            return;
        }
        model.addAttribute("navbarNotifications", notificationService.findRecentForUser(user.getId(), NAVBAR_NOTIF_LIMIT));
    }
}
