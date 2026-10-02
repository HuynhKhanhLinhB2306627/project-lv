package com.healthrecord.healthrecord.service;

import com.healthrecord.healthrecord.entity.ProfileAccess;
import java.util.List;

public interface ProfileAccessService {
    
    List<ProfileAccess> getAccessListByProfileId(Long profileId);

    
    void shareProfile(Long profileId, String emailToShare, String relationship, ProfileAccess.AccessLevel level);

    
    void revokeAccess(Long accessId);

    
    boolean hasEditorAccess(Long userId, Long profileId);
}