package com.healthrecord.healthrecord.controller.api;

import com.healthrecord.healthrecord.dto.VaccinationDto;
import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.entity.Vaccination;
import com.healthrecord.healthrecord.service.HealthProfileService;
import com.healthrecord.healthrecord.service.ProfileAccessService;
import com.healthrecord.healthrecord.service.VaccinationService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vaccinations")
public class VaccinationRestController {

    @Autowired private VaccinationService vaccinationService;
    @Autowired private HealthProfileService healthProfileService;
    @Autowired private ProfileAccessService profileAccessService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getVaccinations(@RequestParam(required = false) Long profileId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(401).build();

        
        List<HealthProfile> profiles = healthProfileService.getAllAccessibleProfiles();
        
        Long currentProfileId = profileId;
        if (currentProfileId == null && !profiles.isEmpty()) {
            currentProfileId = profiles.get(0).getId();
        }

        List<Vaccination> vaccinations = Collections.emptyList();
        boolean canEdit = false;

        if (currentProfileId != null) {
            vaccinations = vaccinationService.findVaccinationsByProfileId(currentProfileId);
            HealthProfile profile = healthProfileService.findProfileById(currentProfileId);
            if (profile != null) {
                boolean isOwner = profile.getUser().getId().equals(currentUser.getId());
                boolean isEditor = profileAccessService.hasEditorAccess(currentUser.getId(), currentProfileId);
                canEdit = isOwner || isEditor;
            }
        }

        
        List<Map<String, Object>> profileList = new ArrayList<>();
        for (HealthProfile p : profiles) {
            Map<String, Object> pMap = new HashMap<>();
            pMap.put("id", p.getId());
            pMap.put("profileName", p.getProfileName());
            pMap.put("fullName", p.getFullName());
            profileList.add(pMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("vaccinations", vaccinations.stream().map(this::convertToMap).collect(Collectors.toList()));
        response.put("canEdit", canEdit);
        response.put("profiles", profileList);
        response.put("currentProfileId", currentProfileId);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> saveVaccination(@Valid @RequestBody VaccinationDto dto) {
        try {
            vaccinationService.saveVaccination(dto);
            return ResponseEntity.ok(Map.of("success", true, "message", "Lưu thông tin tiêm chủng thành công."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVaccination(@PathVariable Long id) {
        try {
            vaccinationService.deleteVaccinationById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa thành công."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private Map<String, Object> convertToMap(Vaccination v) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", v.getId());
        map.put("vaccineName", v.getVaccineName());
        map.put("doseNumber", v.getDoseNumber());
        map.put("vaccinationDate", v.getVaccinationDate());
        map.put("location", v.getLocation());
        return map;
    }
}
