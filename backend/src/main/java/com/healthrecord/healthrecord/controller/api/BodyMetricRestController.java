package com.healthrecord.healthrecord.controller.api;

import com.healthrecord.healthrecord.dto.BodyMetricDto;
import com.healthrecord.healthrecord.entity.BodyMetric;
import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.User;

import com.healthrecord.healthrecord.service.BodyMetricService;
import com.healthrecord.healthrecord.service.HealthProfileService;
import com.healthrecord.healthrecord.service.ProfileAccessService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/body-metrics")
public class BodyMetricRestController {

    @Autowired
    private BodyMetricService bodyMetricService;

    @Autowired
    private HealthProfileService healthProfileService;

    @Autowired
    private ProfileAccessService profileAccessService;


    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getBodyMetrics(@RequestParam(required = false) Long profileId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(401).build();

        List<HealthProfile> profiles = healthProfileService.getAllAccessibleProfiles();
        Long currentProfileId = profileId;
        if (currentProfileId == null && !profiles.isEmpty()) {
            currentProfileId = profiles.get(0).getId();
        }

        List<BodyMetric> bodyMetrics = Collections.emptyList();
        boolean canEdit = false;

        if (currentProfileId != null) {
            bodyMetrics = bodyMetricService.findAllBodyMetricsByProfileId(currentProfileId);
            HealthProfile profile = healthProfileService.findProfileById(currentProfileId);
            if (profile != null) {
                boolean isOwner = profile.getUser().getId().equals(currentUser.getId());
                boolean isEditor = profileAccessService.hasEditorAccess(currentUser.getId(), currentProfileId);
                canEdit = isOwner || isEditor;
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("metrics", bodyMetrics.stream().map(this::convertToMap).collect(Collectors.toList()));
        response.put("canEdit", canEdit);
        response.put("profiles", profiles.stream().map(p -> {
            Map<String, Object> profileMap = new HashMap<>();
            profileMap.put("id", p.getId());
            profileMap.put("profileName", p.getProfileName());
            profileMap.put("fullName", p.getFullName());
            profileMap.put("dob", p.getDateOfBirth() != null ? p.getDateOfBirth().toString() : null);
            return profileMap;
        }).collect(Collectors.toList()));
        response.put("currentProfileId", currentProfileId);

        
        if (bodyMetrics.size() > 0) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            response.put("chartLabels", bodyMetrics.stream().map(m -> m.getLogDate().format(formatter)).collect(Collectors.toList()));
            response.put("chartWeights", bodyMetrics.stream().map(BodyMetric::getWeightInKg).collect(Collectors.toList()));
            response.put("chartBmis", bodyMetrics.stream().map(m -> m.getBmi() != null ? Math.round(m.getBmi() * 10.0) / 10.0 : null).collect(Collectors.toList()));
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> saveBodyMetric(@Valid @RequestBody BodyMetricDto dto) {
        try {
            bodyMetricService.saveOrUpdateBodyMetric(dto);
            return ResponseEntity.ok(Map.of("success", true, "message", "Lưu chỉ số thành công."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBodyMetric(@PathVariable Long id) {
        try {
            bodyMetricService.deleteBodyMetricById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa thành công."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private Map<String, Object> convertToMap(BodyMetric m) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", m.getId());
        map.put("height", m.getHeightInCm());
        map.put("weight", m.getWeightInKg());
        map.put("bmi", m.getBmi() != null ? Math.round(m.getBmi() * 10.0) / 10.0 : null);
        map.put("systolic", m.getSystolicBp());
        map.put("diastolic", m.getDiastolicBp());
        
        if (m.getLogDate() != null) {
            map.put("date", m.getLogDate().toString());
            map.put("formattedDate", m.getLogDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            map.put("formattedTime", m.getLogDate().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        
        map.put("cholesterol", m.getCholesterol());
        map.put("glucose", m.getGlucose());
        map.put("smoke", m.getSmoking());
        map.put("alco", m.getAlcohol());
        map.put("active", m.getPhysicalActivity());
        return map;
    }
}
