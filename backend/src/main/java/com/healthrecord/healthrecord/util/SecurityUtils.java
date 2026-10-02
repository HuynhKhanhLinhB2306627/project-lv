package com.healthrecord.healthrecord.util;

import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private static UserRepository userRepository;

    
    public SecurityUtils(UserRepository userRepository) {
        SecurityUtils.userRepository = userRepository;
    }

    public static User getCurrentUser() {
        if (SecurityContextHolder.getContext().getAuthentication() == null || 
            !SecurityContextHolder.getContext().getAuthentication().isAuthenticated() ||
            "anonymousUser".equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {
            return null;
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return userRepository.findByEmail(username).orElse(null);
    }
}