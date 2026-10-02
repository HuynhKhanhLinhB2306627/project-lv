package com.healthrecord.healthrecord.controller.api;

import com.healthrecord.healthrecord.dto.AppointmentDto;
import com.healthrecord.healthrecord.entity.Appointment;
import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.service.AppointmentService;
import com.healthrecord.healthrecord.service.HealthProfileService;
import com.healthrecord.healthrecord.service.ProfileAccessService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentRestController {

    @Autowired private AppointmentService appointmentService;
    @Autowired private HealthProfileService healthProfileService;
    @Autowired private ProfileAccessService profileAccessService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAppointments(@RequestParam(required = false) Long profileId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(401).build();

        List<HealthProfile> profiles = healthProfileService.getAllAccessibleProfiles();
        Long currentProfileId = profileId;
        if (currentProfileId == null && !profiles.isEmpty()) {
            currentProfileId = profiles.get(0).getId();
        }

        List<Appointment> appointments = Collections.emptyList();
        boolean canEdit = false;

        if (currentProfileId != null) {
            appointments = appointmentService.findAppointmentsByProfileId(currentProfileId);
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
        response.put("appointments", appointments.stream().map(this::convertToMap).collect(Collectors.toList()));
        response.put("canEdit", canEdit);
        response.put("profiles", profileList);
        response.put("currentProfileId", currentProfileId);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> saveAppointment(@Valid @RequestBody AppointmentDto dto) {
        try {
            appointmentService.saveAppointment(dto);
            return ResponseEntity.ok(Map.of("success", true, "message", "Lưu lịch hẹn thành công."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        try {
            appointmentService.deleteAppointmentById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa thành công."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private Map<String, Object> convertToMap(Appointment a) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", a.getId());
        map.put("appointmentDate", a.getAppointmentDate());
        map.put("doctorName", a.getDoctorName());
        map.put("location", a.getLocation());
        map.put("reason", a.getReason());
        map.put("diagnosis", a.getDiagnosis());
        return map;
    }
}
