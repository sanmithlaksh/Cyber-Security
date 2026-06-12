package com.cybershield.portal.repository;

import com.cybershield.portal.model.Notification;
import com.cybershield.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByDateCreatedDesc(User user);
    long countByUserAndReadStatus(User user, boolean readStatus);
}
