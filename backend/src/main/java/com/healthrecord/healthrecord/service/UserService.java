package com.healthrecord.healthrecord.service;

import com.healthrecord.healthrecord.dto.RegistrationDto;
import com.healthrecord.healthrecord.dto.UserDto; 
import com.healthrecord.healthrecord.dto.UserProfileDto;
import com.healthrecord.healthrecord.entity.User;

public interface UserService {
    User registerUser(RegistrationDto registrationDto);
    void updateUserProfile(UserProfileDto userProfileDto);

    
    void updateUserInfo(UserDto userDto);

    User findByEmail(String email);
}