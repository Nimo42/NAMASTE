package com.ayush.terminology.namaste.repo;

import com.ayush.terminology.namaste.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String email);
    List<Notification> findByRecipientEmailAndStatusOrderByCreatedAtDesc(String email, String status);
    long countByRecipientEmailAndStatus(String email, String status);
}
