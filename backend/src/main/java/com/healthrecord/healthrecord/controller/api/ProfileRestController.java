package com.healthrecord.healthrecord.controller.api;

import com.healthrecord.healthrecord.dto.HealthProfileDto;
import com.healthrecord.healthrecord.entity.BodyMetric;
import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.service.HealthProfileService;
import com.healthrecord.healthrecord.service.ProfileAccessService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user-profile")
public class ProfileRestController {

    @Autowired
    private HealthProfileService healthProfileService;

    @Autowired
    private ProfileAccessService profileAccessService;

    @GetMapping("/list")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllProfiles() {
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            if (currentUser == null) return ResponseEntity.status(401).build();

            System.out.println("Fetching profiles for user: " + currentUser.getEmail());
            List<HealthProfile> myProfiles = healthProfileService.findAllProfilesByCurrentUser();
            
            
            if (myProfiles.isEmpty()) {
                System.out.println("Profiles list empty, triggering forced creation...");
                HealthProfileDto d = new HealthProfileDto();
                d.setProfileName("Tôi");
                d.setFullName(currentUser.getFullName());
                d.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));
                d.setGender("Nam");
                d.setEmergencyContactPhone(currentUser.getPhoneNumber());
                healthProfileService.saveProfile(d);
                myProfiles = healthProfileService.findAllProfilesByCurrentUser();
            }

            List<HealthProfile> sharedProfiles = healthProfileService.findSharedProfilesByCurrentUser();

            List<Map<String, Object>> myList = new ArrayList<>();
            for (HealthProfile p : myProfiles) {
                myList.add(convertProfileToMap(p, currentUser));
            }
            
            List<Map<String, Object>> sharedList = new ArrayList<>();
            for (HealthProfile p : sharedProfiles) {
                Map<String, Object> map = convertProfileToMap(p, currentUser);
                
                if (p.getUser() != null) {
                    map.put("ownerName", p.getUser().getFullName());
                }
                
                
                Optional<com.healthrecord.healthrecord.entity.ProfileAccess> access = 
                    p.getAccessibleUsers().stream()
                    .filter(a -> a.getUser().getId().equals(currentUser.getId()))
                    .findFirst();
                
                if (access.isPresent()) {
                    map.put("accessLevel", access.get().getAccessLevel().name());
                    map.put("canEdit", access.get().getAccessLevel() == com.healthrecord.healthrecord.entity.ProfileAccess.AccessLevel.EDITOR);
                } else {
                    map.put("accessLevel", "VIEWER");
                    map.put("canEdit", false);
                }
                
                sharedList.add(map);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("myProfiles", myList);
            response.put("sharedProfiles", sharedList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR IN /list API: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi máy chủ: " + e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createProfile(@RequestBody HealthProfileDto dto) {
        try {
            healthProfileService.saveProfile(dto);
            return ResponseEntity.ok(Map.of("success", true, "message", "Tạo hồ sơ thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveProfileGeneric(@RequestBody HealthProfileDto dto) {
        try {
            healthProfileService.saveProfile(dto);
            return ResponseEntity.ok(Map.of("success", true, "message", "Lưu hồ sơ thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfileById(@PathVariable Long id) {
        try {
            HealthProfile profile = healthProfileService.findProfileById(id);
            if (profile == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(convertProfileToMap(profile, SecurityUtils.getCurrentUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "ID không hợp lệ hoặc không có quyền"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfileById(@PathVariable Long id, @RequestBody HealthProfileDto dto) {
        HealthProfile profile = healthProfileService.findProfileById(id);
        if (profile == null) return ResponseEntity.notFound().build();

        dto.setId(id);
        try {
            healthProfileService.saveProfile(dto);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật hồ sơ thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfileById(@PathVariable Long id) {
        try {
            healthProfileService.deleteProfileById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa hồ sơ thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/access")
    public ResponseEntity<?> getProfileAccess(@PathVariable Long id) {
        HealthProfile profile = healthProfileService.findProfileById(id);
        if (profile == null) return ResponseEntity.notFound().build();
        
        List<com.healthrecord.healthrecord.entity.ProfileAccess> accessList = profileAccessService.getAccessListByProfileId(id);
        List<Map<String, Object>> result = accessList.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("userName", a.getUser().getFullName());
            map.put("userEmail", a.getUser().getEmail());
            map.put("relationship", a.getRelationship());
            map.put("accessLevel", a.getAccessLevel().name());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    
    @GetMapping("/shares/{id}")
    public ResponseEntity<?> getProfileSharesAlias(@PathVariable Long id) {
        return getProfileAccess(id);
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<?> shareProfile(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String relationship = payload.get("relationship");
            com.healthrecord.healthrecord.entity.ProfileAccess.AccessLevel level = 
                com.healthrecord.healthrecord.entity.ProfileAccess.AccessLevel.valueOf(payload.get("accessLevel"));
            
            profileAccessService.shareProfile(id, email, relationship, level);
            return ResponseEntity.ok(Map.of("success", true, "message", "Chia sẻ hồ sơ thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    
    @PostMapping("/share")
    public ResponseEntity<?> shareProfileAlias(@RequestBody Map<String, String> payload) {
        try {
            Long profileId = Long.parseLong(String.valueOf(payload.get("profileId")));
            return shareProfile(profileId, payload);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Dữ liệu không hợp lệ"));
        }
    }

    @DeleteMapping("/access/{accessId}")
    public ResponseEntity<?> revokeAccess(@PathVariable Long accessId) {
        try {
            profileAccessService.revokeAccess(accessId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Thu hồi quyền thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private Map<String, Object> convertProfileToMap(HealthProfile profile, User currentUser) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", profile.getId());
        data.put("profileName", profile.getProfileName());
        data.put("fullName", profile.getFullName());
        data.put("dob", profile.getDateOfBirth());
        data.put("gender", profile.getGender());
        data.put("bloodType", profile.getBloodType());
        data.put("chronicConditions", profile.getChronicConditions());
        data.put("emergencyContactName", profile.getEmergencyContactName());
        data.put("emergencyContactPhone", profile.getEmergencyContactPhone());
        data.put("avatar", profile.getAvatar());
        
        
        if (currentUser != null) {
            boolean isOwner = profile.getUser().getId().equals(currentUser.getId());
            data.put("isOwner", isOwner);
            data.put("canEdit", isOwner || profileAccessService.hasEditorAccess(currentUser.getId(), profile.getId()));
        } else {
            data.put("isOwner", false);
            data.put("canEdit", false);
        }
        
        return data;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        HealthProfile profile = healthProfileService.findPersonalProfile();
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", profile.getId());
        data.put("fullName", profile.getFullName());
        data.put("dob", profile.getDateOfBirth());
        data.put("email", profile.getUser().getEmail());
        data.put("phone", profile.getEmergencyContactPhone());
        data.put("gender", profile.getGender());
        data.put("bloodType", profile.getBloodType());
        data.put("chronicConditions", profile.getChronicConditions());
        data.put("avatar", profile.getAvatar());

        
        profile.getBodyMetrics().stream()
                .max(Comparator.comparing(BodyMetric::getLogDate))
                .ifPresent(metric -> {
                    data.put("height", metric.getHeightInCm());
                    data.put("weight", metric.getWeightInKg());
                    data.put("bmi", metric.getBmi());
                });

        return ResponseEntity.ok(data);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody HealthProfileDto dto) {
        HealthProfile profile = healthProfileService.findPersonalProfile();
        if (profile == null) return ResponseEntity.notFound().build();

        dto.setId(profile.getId());
        dto.setProfileName(profile.getProfileName()); 
        
        try {
            healthProfileService.saveProfile(dto);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật hồ sơ thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        HealthProfile profile = healthProfileService.findPersonalProfile();
        if (profile == null) return ResponseEntity.notFound().build();

        HealthProfileDto dto = new HealthProfileDto();
        dto.setId(profile.getId());
        dto.setProfileName(profile.getProfileName());
        dto.setFullName(profile.getFullName());
        dto.setDateOfBirth(profile.getDateOfBirth());
        dto.setGender(profile.getGender());
        dto.setBloodType(profile.getBloodType());
        dto.setChronicConditions(profile.getChronicConditions());
        dto.setEmergencyContactName(profile.getEmergencyContactName());
        dto.setEmergencyContactPhone(profile.getEmergencyContactPhone());
        dto.setAvatarFile(file);

        try {
            healthProfileService.saveProfile(dto);
            HealthProfile updated = healthProfileService.findProfileById(profile.getId());
            return ResponseEntity.ok(Map.of("success", true, "avatar", updated.getAvatar()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
