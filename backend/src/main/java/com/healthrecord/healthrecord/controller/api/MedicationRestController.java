package com.healthrecord.healthrecord.controller.api;

import com.healthrecord.healthrecord.dto.MedicationCourseDto;
import com.healthrecord.healthrecord.entity.*;
import com.healthrecord.healthrecord.repository.MedicineRepository;
import com.healthrecord.healthrecord.service.HealthProfileService;
import com.healthrecord.healthrecord.service.MedicationService;
import com.healthrecord.healthrecord.service.ProfileAccessService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medications")
public class MedicationRestController {

    @Autowired private MedicationService medicationService;
    @Autowired private HealthProfileService healthProfileService;
    @Autowired private ProfileAccessService profileAccessService;
    @Autowired private MedicineRepository medicineRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getMedicationData(@RequestParam(required = false) Long profileId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(401).build();

        List<HealthProfile> profiles = healthProfileService.getAllAccessibleProfiles();
        
        Long currentProfileId = profileId;
        if (currentProfileId == null && !profiles.isEmpty()) {
            currentProfileId = profiles.get(0).getId();
        }

        List<MedicationCourse> courses = Collections.emptyList();
        boolean canEdit = false;

        if (currentProfileId != null) {
            courses = medicationService.findAllCoursesByProfileId(currentProfileId);
            HealthProfile profile = healthProfileService.findProfileById(currentProfileId);
            if (profile != null) {
                boolean isOwner = profile.getUser().getId().equals(currentUser.getId());
                boolean isEditor = profileAccessService.hasEditorAccess(currentUser.getId(), currentProfileId);
                canEdit = isOwner || isEditor;
            }
        }

        List<Map<String, Object>> profileList = profiles.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("profileName", p.getProfileName());
            map.put("fullName", p.getFullName());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("courses", courses.stream().map(this::convertCourseToMap).collect(Collectors.toList()));
        response.put("canEdit", canEdit);
        response.put("profiles", profileList);
        response.put("currentProfileId", currentProfileId);
        
        
        response.put("medicines", medicineRepository.findByIsApprovedTrue().stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("name", m.getName());
            map.put("unit", m.getUnit());
            return map;
        }).collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> saveCourse(@RequestBody MedicationCourseDto dto) {
        try {
            if (dto.getId() != null) {
                medicationService.updateMedicationCourse(dto.getId(), dto);
                return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật liệu trình thành công"));
            } else {
                medicationService.saveMedicationCourse(dto);
                return ResponseEntity.ok(Map.of("success", true, "message", "Thêm liệu trình thành công"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        try {
            medicationService.deleteCourseById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/today-reminders")
    public ResponseEntity<?> getTodayReminders(@RequestParam(required = false) Long profileId) {
        List<MedicationReminder> reminders = medicationService.findTodayRemindersByCurrentUser();
        if (profileId != null) {
            reminders = reminders.stream()
                    .filter(r -> r.getMedicationCourse().getHealthProfile().getId().equals(profileId))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(reminders.stream().map(this::convertReminderToMap).collect(Collectors.toList()));
    }

    @PostMapping("/reminders/{id}/status")
    public ResponseEntity<?> updateReminderStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            MedicationReminder.ReminderStatus status = MedicationReminder.ReminderStatus.valueOf(payload.get("status"));
            medicationService.updateReminderStatus(id, status);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private Map<String, Object> convertCourseToMap(MedicationCourse c) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", c.getId());
        map.put("medicineName", c.getMedicine().getName());
        map.put("medicineId", c.getMedicine().getId());
        map.put("unit", c.getMedicine().getUnit());
        map.put("dosage", c.getDosage());
        map.put("frequency", c.getFrequency());
        map.put("timing", c.getTiming());
        map.put("notes", c.getNotes());
        map.put("startDate", c.getStartDate());
        map.put("endDate", c.getEndDate());
        map.put("status", c.getStatus());
        
        
        long duration = java.time.temporal.ChronoUnit.DAYS.between(c.getStartDate(), c.getEndDate()) + 1;
        map.put("duration", duration);

        return map;
    }

    private Map<String, Object> convertReminderToMap(MedicationReminder r) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", r.getId());
        map.put("time", r.getReminderTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        map.put("status", r.getStatus());
        map.put("medicineName", r.getMedicationCourse().getMedicine().getName());
        map.put("dosage", r.getMedicationCourse().getDosage());
        map.put("timing", r.getMedicationCourse().getTiming());
        map.put("profileName", r.getMedicationCourse().getHealthProfile().getProfileName());
        return map;
    }
}
