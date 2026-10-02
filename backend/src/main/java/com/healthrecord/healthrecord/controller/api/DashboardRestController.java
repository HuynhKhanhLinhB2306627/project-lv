package com.healthrecord.healthrecord.controller.api;

import com.healthrecord.healthrecord.entity.*;
import com.healthrecord.healthrecord.service.*;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    @Autowired
    private HealthProfileService healthProfileService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private VaccinationService vaccinationService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private InsuranceCardService insuranceCardService;

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(@RequestParam(required = false) Long profileId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(401).build();

        List<HealthProfile> accessibleProfiles = healthProfileService.getAllAccessibleProfiles();
        if (accessibleProfiles.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyMap());
        }

        
        HealthProfile targetProfile;
        if (profileId != null) {
            targetProfile = accessibleProfiles.stream()
                    .filter(p -> p.getId().equals(profileId))
                    .findFirst()
                    .orElse(accessibleProfiles.get(0));
        } else {
            targetProfile = accessibleProfiles.get(0);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("currentProfileId", targetProfile.getId());
        stats.put("profileName", targetProfile.getFullName());

        
        stats.put("appointmentCount", targetProfile.getAppointments().size());
        stats.put("medicationCount", targetProfile.getMedicationCourses().size());
        stats.put("vaccinationCount", targetProfile.getVaccinations().size());
        stats.put("documentCount", targetProfile.getDocuments().size());
        stats.put("insuranceCount", targetProfile.getInsuranceCards().size());

        
        targetProfile.getBodyMetrics().stream()
                .max(Comparator.comparing(BodyMetric::getLogDate))
                .ifPresent(m -> {
                    Map<String, Object> latest = new HashMap<>();
                    
                    latest.put("heartRate", "--"); 
                    latest.put("systolicBP", m.getSystolicBp());
                    latest.put("diastolicBP", m.getDiastolicBp());
                    latest.put("weight", m.getWeightInKg());
                    latest.put("bmi", m.getBmi());
                    stats.put("latestMetric", latest);
                });

        
        List<BodyMetric> recentMetrics = targetProfile.getBodyMetrics().stream()
                .sorted(Comparator.comparing(BodyMetric::getLogDate).reversed())
                .limit(10)
                .collect(Collectors.toList());

        if (!recentMetrics.isEmpty()) {
            double avgBmi = recentMetrics.stream().mapToDouble(BodyMetric::getBmi).average().orElse(0);
            double avgSys = recentMetrics.stream().filter(m -> m.getSystolicBp() != null).mapToDouble(BodyMetric::getSystolicBp).average().orElse(0);
            double avgDia = recentMetrics.stream().filter(m -> m.getDiastolicBp() != null).mapToDouble(BodyMetric::getDiastolicBp).average().orElse(0);
            
            stats.put("avgBmi", Math.round(avgBmi * 10.0) / 10.0);
            stats.put("avgSystolic", (int) avgSys);
            stats.put("avgDiastolic", (int) avgDia);

            
            stats.put("chartLabels", recentMetrics.stream().map(m -> m.getLogDate().toLocalDate().toString()).collect(Collectors.toList()));
            stats.put("chartBmis", recentMetrics.stream().map(BodyMetric::getBmi).collect(Collectors.toList()));
            stats.put("chartWeights", recentMetrics.stream().map(BodyMetric::getWeightInKg).collect(Collectors.toList()));
        }

        
        java.time.LocalDate today = java.time.LocalDate.now();
        List<Map<String, Object>> todayApps = targetProfile.getAppointments().stream()
                .filter(a -> a.getAppointmentDate() != null && a.getAppointmentDate().equals(today))
                .map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", a.getId());
                    
                    map.put("time", "Cả ngày"); 
                    map.put("reason", a.getReason());
                    map.put("location", a.getLocation());
                    map.put("doctorName", a.getDoctorName());
                    return map;
                }).collect(Collectors.toList());
        stats.put("todayAppointments", todayApps);

        return ResponseEntity.ok(stats);
    }
}
