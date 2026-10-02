package com.healthrecord.healthrecord.controller;

import com.healthrecord.healthrecord.entity.Notification;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.repository.NotificationRepository;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getMyNotifications() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return ResponseEntity.ok(List.of());

        
        List<Notification> list = notificationRepository.findTop10ByRecipientOrderByCreatedAtDesc(currentUser);

        
        List<Map<String, Object>> result = list.stream().map(n -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", n.getId());
            map.put("message", n.getMessage());
            map.put("link", n.getLink());
            map.put("read", n.isRead());
            
            map.put("time", n.getCreatedAt().toString());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<String> markAllRead() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(401).build();

        List<Notification> unread = notificationRepository.findByRecipientAndIsReadFalse(currentUser);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);

        return ResponseEntity.ok("Success");
    }

    @PostMapping("/mark-read/{id}")
    public ResponseEntity<String> markRead(@PathVariable Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(401).build();

        notificationRepository.findById(id).ifPresent(n -> {
            if (n.getRecipient().getId().equals(currentUser.getId())) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });

        return ResponseEntity.ok("Success");
    }
}