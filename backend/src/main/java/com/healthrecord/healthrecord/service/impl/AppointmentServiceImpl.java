package com.healthrecord.healthrecord.service.impl;

import com.healthrecord.healthrecord.dto.AppointmentDto;
import com.healthrecord.healthrecord.entity.Appointment;
import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.repository.AppointmentRepository;
import com.healthrecord.healthrecord.repository.HealthProfileRepository;
import com.healthrecord.healthrecord.service.AppointmentService;
import com.healthrecord.healthrecord.service.ProfileAccessService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private HealthProfileRepository healthProfileRepository;
    @Autowired private ProfileAccessService profileAccessService;

    
    private HealthProfile validateProfileAccess(Long profileId, boolean requireEditorRole) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || profileId == null) return null;

        HealthProfile profile = healthProfileRepository.findById(profileId).orElse(null);
        if (profile == null) return null;

        boolean isOwner = profile.getUser().getId().equals(currentUser.getId());

        
        if (isOwner) return profile;

        
        if (requireEditorRole) {
            boolean isEditor = profileAccessService.hasEditorAccess(currentUser.getId(), profileId);
            if (!isEditor) {
                throw new SecurityException("Bạn không có quyền chỉnh sửa hồ sơ này.");
            }
        } else {
            
            boolean hasAccess = profileAccessService.getAccessListByProfileId(profileId).stream()
                    .anyMatch(access -> access.getUser().getId().equals(currentUser.getId()));
            if (!hasAccess) return null;
        }

        return profile;
    }

    @Override
    public List<Appointment> findAppointmentsByProfileId(Long profileId) {
        HealthProfile profile = validateProfileAccess(profileId, false); 
        if (profile != null) {
            return appointmentRepository.findByHealthProfileOrderByAppointmentDateDesc(profile);
        }
        return Collections.emptyList();
    }

    @Override
    public void saveAppointment(AppointmentDto dto) {
        HealthProfile profile = validateProfileAccess(dto.getProfileId(), true); 
        if (profile == null) {
            throw new IllegalArgumentException("Hồ sơ không tồn tại hoặc không có quyền truy cập");
        }

        Appointment appointment;
        if (dto.getId() != null) {
            appointment = appointmentRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Cuộc hẹn không tồn tại"));

            
            if (!appointment.getHealthProfile().getId().equals(profile.getId())) {
                throw new SecurityException("Dữ liệu không khớp với hồ sơ.");
            }
        } else {
            appointment = new Appointment();
            appointment.setHealthProfile(profile);
        }

        appointment.setAppointmentDate(dto.getAppointmentDate());
        appointment.setDoctorName(dto.getDoctorName());
        appointment.setLocation(dto.getLocation());
        appointment.setReason(dto.getReason());
        appointment.setDiagnosis(dto.getDiagnosis());

        appointmentRepository.save(appointment);
    }

    @Override
    public Appointment findAppointmentById(Long id) {
        Appointment appt = appointmentRepository.findById(id).orElse(null);
        if (appt == null) return null;

        
        HealthProfile profile = validateProfileAccess(appt.getHealthProfile().getId(), false);
        return (profile != null) ? appt : null;
    }

    @Override
    public void deleteAppointmentById(Long id) {
        Appointment appt = appointmentRepository.findById(id).orElse(null);
        if (appt != null) {
            
            validateProfileAccess(appt.getHealthProfile().getId(), true);
            appointmentRepository.deleteById(id);
        }
    }

    @Override
    public long countAppointmentsByProfileId(Long profileId) {
        HealthProfile profile = validateProfileAccess(profileId, false);
        return (profile != null) ? appointmentRepository.countByHealthProfile(profile) : 0;
    }

    @Override
    public Map<String, Long> getMonthlyAppointmentCounts(Long profileId) {
        HealthProfile profile = validateProfileAccess(profileId, false);
        if (profile == null) return Collections.emptyMap();

        List<Object[]> results = appointmentRepository.countAppointmentsByMonth(profile);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1],
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }
}