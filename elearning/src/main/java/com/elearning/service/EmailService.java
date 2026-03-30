package com.elearning.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Gửi email khi đã cấu hình {@code spring.mail.host}; nếu không — ghi log (dev).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${app.mail.from:}")
    private String fromOverride;

    public void sendPlainText(String to, String subject, String text) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("[Email not configured — set spring.mail.host] To: {}\nSubject: {}\n{}", to, subject, text);
            return;
        }
        String from = (fromOverride != null && !fromOverride.isEmpty()) ? fromOverride : fromAddress;
        if (from == null || from.isEmpty()) {
            from = "noreply@elearning.local";
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
        log.debug("Sent email to {}", to);
    }
}
