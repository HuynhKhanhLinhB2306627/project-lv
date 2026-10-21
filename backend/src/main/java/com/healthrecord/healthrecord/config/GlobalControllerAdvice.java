package com.healthrecord.healthrecord.config;

import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.service.HealthProfileService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private HealthProfileService healthProfileService;

    @Autowired
    private com.healthrecord.healthrecord.repository.NotificationRepository notificationRepository;

    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        return SecurityUtils.getCurrentUser();
    }

    @ModelAttribute("currentUserId")
    public Long getCurrentUserId() {
        User user = SecurityUtils.getCurrentUser();
        return (user != null) ? user.getId() : null;
    }

    @ModelAttribute("unreadNotificationCount")
    public long getUnreadNotificationCount() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) return 0;
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }

    
    @ModelAttribute("globalProfiles")
    public List<HealthProfile> getGlobalProfiles() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) {
            return Collections.emptyList();
        }
        
        return healthProfileService.getAllAccessibleProfiles();
    }
}