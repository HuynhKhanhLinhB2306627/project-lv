package com.healthrecord.healthrecord.service;

import com.healthrecord.healthrecord.dto.HealthProfileDto;
import com.healthrecord.healthrecord.entity.HealthProfile;
import java.util.List;

public interface HealthProfileService {
    List<HealthProfile> findAllProfilesByCurrentUser();

    
    List<HealthProfile> findSharedProfilesByCurrentUser();

    
    
    List<HealthProfile> getAllAccessibleProfiles();

    void saveProfile(HealthProfileDto profileDto);
    HealthProfile findProfileById(Long id);
    HealthProfile findPersonalProfile();
    void deleteProfileById(Long id);
    void resetProfileData(Long profileId);
}