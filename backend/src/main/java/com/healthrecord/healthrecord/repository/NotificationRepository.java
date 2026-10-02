package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.Notification;
import com.healthrecord.healthrecord.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findTop10ByRecipientOrderByCreatedAtDesc(User recipient);

    
    long countByRecipientAndIsReadFalse(User recipient);

    
    List<Notification> findByRecipientAndIsReadFalse(User recipient);
}