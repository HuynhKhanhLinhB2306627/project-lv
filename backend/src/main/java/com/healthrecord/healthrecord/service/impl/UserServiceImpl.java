package com.healthrecord.healthrecord.service.impl;

import com.healthrecord.healthrecord.dto.RegistrationDto;
import com.healthrecord.healthrecord.dto.UserDto; 
import com.healthrecord.healthrecord.dto.UserProfileDto;
import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.repository.HealthProfileRepository;
import com.healthrecord.healthrecord.repository.UserRepository;
import com.healthrecord.healthrecord.service.UserService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private HealthProfileRepository healthProfileRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User registerUser(RegistrationDto dto) {
        
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already exists!");
        }
        User newUser = new User();
        newUser.setFullName(dto.getFullName());
        newUser.setEmail(dto.getEmail());
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));

        
        newUser.setPhoneNumber(dto.getPhoneNumber());

        User savedUser = userRepository.save(newUser);

        HealthProfile profile = new HealthProfile();
        profile.setUser(savedUser);
        profile.setProfileName("Tôi");
        profile.setFullName(dto.getFullName());
        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setEmergencyContactPhone(dto.getPhoneNumber());

        healthProfileRepository.save(profile);
        return savedUser;
    }

    @Override
    public void updateUserProfile(UserProfileDto userProfileDto) {
        
    }

    
    @Override
    @Transactional
    public void updateUserInfo(UserDto userDto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            currentUser.setFullName(userDto.getFullName());
            currentUser.setPhoneNumber(userDto.getPhoneNumber());
            
            userRepository.save(currentUser);
        }
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}