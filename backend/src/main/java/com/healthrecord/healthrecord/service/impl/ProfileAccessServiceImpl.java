package com.healthrecord.healthrecord.service.impl;

import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.ProfileAccess;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.repository.HealthProfileRepository;
import com.healthrecord.healthrecord.repository.ProfileAccessRepository;
import com.healthrecord.healthrecord.repository.UserRepository;
import com.healthrecord.healthrecord.service.ProfileAccessService;
import com.healthrecord.healthrecord.service.NotificationService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileAccessServiceImpl implements ProfileAccessService {

    @Autowired
    private ProfileAccessRepository profileAccessRepository;

    @Autowired
    private HealthProfileRepository healthProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<ProfileAccess> getAccessListByProfileId(Long profileId) {
        return profileAccessRepository.findByHealthProfileId(profileId);
    }

    
    @Override
    public boolean hasEditorAccess(Long userId, Long profileId) {
        
        Optional<ProfileAccess> access = profileAccessRepository.findByHealthProfileIdAndUserId(profileId, userId);
        
        return access.isPresent() && access.get().getAccessLevel() == ProfileAccess.AccessLevel.EDITOR;
    }

    @Autowired
    private com.healthrecord.healthrecord.repository.NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void shareProfile(Long profileId, String emailToShare, String relationship, ProfileAccess.AccessLevel level) {
        User currentUser = SecurityUtils.getCurrentUser();

        HealthProfile profile = healthProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Hồ sơ không tồn tại"));

        if (!profile.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Bạn không phải chủ sở hữu hồ sơ này");
        }

        User targetUser = userRepository.findByEmail(emailToShare)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với email: " + emailToShare));

        if (targetUser.getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Không thể tự chia sẻ cho chính mình");
        }

        Optional<ProfileAccess> existingAccess = profileAccessRepository
                .findByHealthProfileIdAndUserId(profileId, targetUser.getId());

        if (existingAccess.isPresent()) {
            throw new IllegalArgumentException("Người này đã được chia sẻ hồ sơ rồi.");
        }

        ProfileAccess access = new ProfileAccess();
        access.setHealthProfile(profile);
        access.setUser(targetUser);
        access.setAccessLevel(level);
        access.setRelationship(relationship);
        access.setStatus(ProfileAccess.AccessStatus.ACTIVE);

        profileAccessRepository.save(access);

        
        notificationService.createAndSendNotification(targetUser, currentUser.getFullName() + " đã chia sẻ hồ sơ '" + profile.getProfileName() + "' với bạn.", "/profiles/" + profileId);
    }

    @Override
    @Transactional
    public void revokeAccess(Long accessId) {
        User currentUser = SecurityUtils.getCurrentUser();
        ProfileAccess access = profileAccessRepository.findById(accessId)
                .orElseThrow(() -> new IllegalArgumentException("Quyền truy cập không tồn tại"));

        boolean isOwner = access.getHealthProfile().getUser().getId().equals(currentUser.getId());
        boolean isRecipient = access.getUser().getId().equals(currentUser.getId());

        if (!isOwner && !isRecipient) {
            throw new SecurityException("Bạn không có quyền thu hồi quyền truy cập này");
        }

        profileAccessRepository.delete(access);
    }
}