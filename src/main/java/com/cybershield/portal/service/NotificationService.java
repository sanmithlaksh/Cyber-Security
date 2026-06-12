package com.cybershield.portal.service;

import com.cybershield.portal.model.Notification;
import com.cybershield.portal.model.User;
import com.cybershield.portal.repository.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public Notification createNotification(User user, String message) {
        Notification notification = new Notification(user, message, false, LocalDateTime.now());
        Notification saved = notificationRepository.save(notification);

        // Push real-time notification to the user's specific WebSocket queue
        try {
            messagingTemplate.convertAndSendToUser(
                    user.getEmail(),
                    "/queue/notifications",
                    new NotificationDto(saved.getId(), saved.getMessage(), saved.getDateCreated().toString())
            );
        } catch (Exception e) {
            System.err.println("Could not send real-time WebSocket notification: " + e.getMessage());
        }

        return saved;
    }

    public List<Notification> getNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByDateCreatedDesc(user);
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndReadStatus(user, false);
    }

    public void markAllAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByUserOrderByDateCreatedDesc(user);
        for (Notification n : notifications) {
            if (!n.isReadStatus()) {
                n.setReadStatus(true);
                notificationRepository.save(n);
            }
        }
    }

    public static class NotificationDto {
        private final Long id;
        private final String message;
        private final String dateCreated;

        public NotificationDto(Long id, String message, String dateCreated) {
            this.id = id;
            this.message = message;
            this.dateCreated = dateCreated;
        }

        public Long getId() {
            return id;
        }

        public String getMessage() {
            return message;
        }

        public String getDateCreated() {
            return dateCreated;
        }
    }
}
