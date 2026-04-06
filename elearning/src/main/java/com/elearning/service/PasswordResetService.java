package com.elearning.service;

import com.elearning.model.entity.User;
import com.elearning.repository.UserRepository;
import com.elearning.validation.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int TOKEN_HOURS_VALID = 1;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Luôn thành công về mặt thông điệp (không tiết lộ email có tồn tại hay không).
     */
    @Transactional
    public void requestReset(String emailRaw) {
        if (emailRaw == null || emailRaw.trim().isEmpty()) {
            return;
        }
        String email = emailRaw.trim();
        Optional<User> opt = userRepository.findByEmail(email);
        if (!opt.isPresent()) {
            return;
        }
        User user = opt.get();
        if (!user.isActive() || user.isLocked()) {
            return;
        }
        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetExpires(LocalDateTime.now().plusHours(TOKEN_HOURS_VALID));
        userRepository.save(user);

        String link = baseUrl.replaceAll("/$", "") + "/reset-password?token=" + token;
        String body = "You requested a password reset.\n\nOpen this link (valid " + TOKEN_HOURS_VALID + " hour(s)):\n"
                + link + "\n\nIf you did not request this, ignore this message.";
        emailService.sendPlainText(user.getEmail(), "Password reset — E-Learning", body);
    }

    public Optional<User> validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return Optional.empty();
        }
        Optional<User> opt = userRepository.findByPasswordResetToken(token);
        if (!opt.isPresent()) {
            return Optional.empty();
        }
        User u = opt.get();
        if (u.getPasswordResetExpires() == null || LocalDateTime.now().isAfter(u.getPasswordResetExpires())) {
            return Optional.empty();
        }
        return Optional.of(u);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = validateToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link."));
        String err = PasswordPolicy.validate(newPassword).orElse(null);
        if (err != null) {
            throw new IllegalArgumentException(err);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        userRepository.save(user);
    }
}
