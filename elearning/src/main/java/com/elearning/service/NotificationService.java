package com.elearning.service;

import com.elearning.model.entity.Notification;
import com.elearning.model.entity.User;
import com.elearning.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> findByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> findRecentForUser(Long userId, int limit) {
        int cap = Math.max(1, Math.min(limit, 25));
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, cap));
    }

    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    @Transactional
    public Notification send(User user, String title, String message, Notification.NotifType type) {
        Notification n = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .build();
        return notificationRepository.save(n);
    }
}
