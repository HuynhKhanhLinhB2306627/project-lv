package com.healthrecord.healthrecord.service.impl;

import com.healthrecord.healthrecord.dto.HealthProfileDto;
import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.ProfileAccess;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.repository.HealthProfileRepository;
import com.healthrecord.healthrecord.repository.ProfileAccessRepository;
import com.healthrecord.healthrecord.service.HealthProfileService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HealthProfileServiceImpl implements HealthProfileService {

    @Autowired
    private HealthProfileRepository healthProfileRepository;

    @Autowired
    private ProfileAccessRepository profileAccessRepository;

    public static final String UPLOAD_DIRECTORY = "uploads";

    @Override
    @Transactional
    public List<HealthProfile> findAllProfilesByCurrentUser() {
        User currentUser = SecurityUtils.getCurrentUser();
        System.out.println("findAllProfilesByCurrentUser for user: " + (currentUser != null ? currentUser.getEmail() : "NULL"));
        
        if (currentUser != null) {
            List<HealthProfile> profiles = healthProfileRepository.findByUserId(currentUser.getId());
            System.out.println("Found " + profiles.size() + " profiles in DB");
            
            
            if (profiles.isEmpty()) {
                System.out.println("Creating default profile 'Tôi' for user " + currentUser.getEmail());
                HealthProfile defaultProfile = new HealthProfile();
                defaultProfile.setUser(currentUser);
                defaultProfile.setProfileName("Tôi");
                defaultProfile.setFullName(currentUser.getFullName() != null ? currentUser.getFullName() : "Người dùng");
                
                defaultProfile.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1)); 
                defaultProfile.setGender("Nam");
                defaultProfile.setEmergencyContactPhone(currentUser.getPhoneNumber());
                
                try {
                    HealthProfile saved = healthProfileRepository.save(defaultProfile);
                    System.out.println("Saved default profile with ID: " + saved.getId());
                    
                    List<HealthProfile> newList = new ArrayList<>();
                    newList.add(saved);
                    return newList;
                } catch (Exception e) {
                    System.err.println("FAILED TO SAVE DEFAULT PROFILE: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            return profiles;
        }
        return Collections.emptyList();
    }

    @Override
    public List<HealthProfile> findSharedProfilesByCurrentUser() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return Collections.emptyList();

        List<ProfileAccess> accessList = profileAccessRepository.findByUserId(currentUser.getId());

        return accessList.stream()
                .map(ProfileAccess::getHealthProfile)
                .collect(Collectors.toList());
    }

    
    @Override
    public List<HealthProfile> getAllAccessibleProfiles() {
        List<HealthProfile> myProfiles = findAllProfilesByCurrentUser();
        List<HealthProfile> sharedProfiles = findSharedProfilesByCurrentUser();

        
        List<HealthProfile> allProfiles = new ArrayList<>(myProfiles);
        allProfiles.addAll(sharedProfiles);

        return allProfiles;
    }

    @Override
    public void saveProfile(HealthProfileDto profileDto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return;

        HealthProfile profile;
        if (profileDto.getId() != null) {
            profile = healthProfileRepository.findById(profileDto.getId())
                    .orElseThrow(() -> new IllegalStateException("Hồ sơ không tồn tại"));

            boolean isOwner = profile.getUser().getId().equals(currentUser.getId());
            boolean isEditor = false;

            if (!isOwner) {
                Optional<ProfileAccess> access = profileAccessRepository.findByHealthProfileIdAndUserId(profile.getId(), currentUser.getId());
                isEditor = access.isPresent() && access.get().getAccessLevel() == ProfileAccess.AccessLevel.EDITOR;
            }

            if (!isOwner && !isEditor) {
                throw new IllegalStateException("Không có quyền sửa hồ sơ này");
            }
        } else {
            profile = new HealthProfile();
            profile.setUser(currentUser);
        }

        if (profileDto.getProfileName() != null) {
            profile.setProfileName(profileDto.getProfileName());
        }
        if (profileDto.getFullName() != null) {
            profile.setFullName(profileDto.getFullName());
        }
        if (profileDto.getDateOfBirth() != null) {
            profile.setDateOfBirth(profileDto.getDateOfBirth());
        }
        if (profileDto.getGender() != null) {
            profile.setGender(profileDto.getGender());
        }
        if (profileDto.getBloodType() != null) {
            profile.setBloodType(profileDto.getBloodType());
        }
        if (profileDto.getChronicConditions() != null) {
            profile.setChronicConditions(profileDto.getChronicConditions());
        }
        if (profileDto.getEmergencyContactName() != null) {
            profile.setEmergencyContactName(profileDto.getEmergencyContactName());
        }
        if (profileDto.getEmergencyContactPhone() != null) {
            profile.setEmergencyContactPhone(profileDto.getEmergencyContactPhone());
        }

        MultipartFile file = profileDto.getAvatarFile();
        if (file != null && !file.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                profile.setAvatar(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu ảnh đại diện", e);
            }
        }

        healthProfileRepository.save(profile);

        
        if ("Tôi".equals(profile.getProfileName())) {
            User user = profile.getUser();
            user.setFullName(profile.getFullName());
        }
    }

    @Override
    public HealthProfile findPersonalProfile() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return null;
        
        List<HealthProfile> profiles = healthProfileRepository.findByUserId(currentUser.getId());
        return profiles.stream()
                .filter(p -> "Tôi".equals(p.getProfileName()))
                .findFirst()
                .orElse(profiles.isEmpty() ? null : profiles.get(0));
    }

    @Override
    public HealthProfile findProfileById(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        HealthProfile profile = healthProfileRepository.findById(id).orElse(null);

        if (profile == null) return null;

        if (profile.getUser().getId().equals(currentUser.getId())) {
            return profile;
        }

        Optional<ProfileAccess> access = profileAccessRepository.findByHealthProfileIdAndUserId(id, currentUser.getId());
        if (access.isPresent()) {
            return profile;
        }

        return null;
    }

    @Override
    @Transactional
    public void deleteProfileById(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) throw new SecurityException("Vui lòng đăng nhập");

        HealthProfile profile = healthProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Hồ sơ không tồn tại"));

        if (!profile.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Bạn không có quyền xóa hồ sơ này");
        }

        
        if ("Tôi".equals(profile.getProfileName())) {
            throw new IllegalStateException("Không thể xóa hồ sơ mặc định của bạn");
        }

        
        profileAccessRepository.deleteByHealthProfileId(id);

        
        
        healthProfileRepository.delete(profile);
    }

    @Override
    @Transactional
    public void resetProfileData(Long profileId) {
        User currentUser = SecurityUtils.getCurrentUser();

        HealthProfile profile = healthProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalStateException("Hồ sơ không tồn tại"));

        if (!profile.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Không có quyền reset hồ sơ này");
        }

        if (profile.getAppointments() != null) profile.getAppointments().clear();
        if (profile.getVaccinations() != null) profile.getVaccinations().clear();
        if (profile.getDocuments() != null) profile.getDocuments().clear();
        if (profile.getBodyMetrics() != null) profile.getBodyMetrics().clear();
        if (profile.getMedicationCourses() != null) profile.getMedicationCourses().clear();
        if (profile.getInsuranceCards() != null) profile.getInsuranceCards().clear();
        if (profile.getAllergies() != null) profile.getAllergies().clear();

        profile.setChronicConditions(null);
        healthProfileRepository.save(profile);
    }
}
