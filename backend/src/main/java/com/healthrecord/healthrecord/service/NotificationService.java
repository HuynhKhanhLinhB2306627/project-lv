package com.healthrecord.healthrecord.service;

import com.healthrecord.healthrecord.entity.Notification;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void createAndSendNotification(User recipient, String message, String link) {
        if (recipient == null) return;

        
        Notification noti = new Notification();
        noti.setRecipient(recipient);
        noti.setMessage(message);
        noti.setLink(link);
        notificationRepository.save(noti);

        
        long unreadCount = notificationRepository.countByRecipientAndIsReadFalse(recipient);

        
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", noti.getId());
        payload.put("message", message);
        payload.put("link", link);
        payload.put("unreadCount", unreadCount);
        payload.put("time", noti.getCreatedAt().toString());

        messagingTemplate.convertAndSend("/topic/notifications/" + recipient.getId(), payload);
    }

    public long getUnreadCount(User user) {
        if (user == null) return 0;
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }
}