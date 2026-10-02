package com.healthrecord.healthrecord.service.impl;

import com.healthrecord.healthrecord.dto.VaccinationDto;
import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.entity.Vaccination;
import com.healthrecord.healthrecord.repository.HealthProfileRepository;
import com.healthrecord.healthrecord.repository.VaccinationRepository;
import com.healthrecord.healthrecord.service.ProfileAccessService;
import com.healthrecord.healthrecord.service.VaccinationService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VaccinationServiceImpl implements VaccinationService {

    @Autowired private VaccinationRepository vaccinationRepository;
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
    public List<Vaccination> findVaccinationsByProfileId(Long profileId) {
        
        HealthProfile profile = validateProfileAccess(profileId, false);
        if (profile != null) {
            return vaccinationRepository.findByHealthProfileOrderByVaccinationDateDesc(profile);
        }
        return Collections.emptyList();
    }

    @Override
    public void saveVaccination(VaccinationDto dto) {
        
        HealthProfile profile = validateProfileAccess(dto.getProfileId(), true);
        if (profile == null) {
            throw new IllegalArgumentException("Hồ sơ không tồn tại hoặc không có quyền truy cập.");
        }

        Vaccination vaccination;
        if (dto.getId() != null) {
            vaccination = vaccinationRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Dữ liệu tiêm chủng không tồn tại"));

            
            if (!vaccination.getHealthProfile().getId().equals(profile.getId())) {
                throw new SecurityException("Dữ liệu không khớp với hồ sơ.");
            }
        } else {
            vaccination = new Vaccination();
            vaccination.setHealthProfile(profile);
        }

        vaccination.setVaccineName(dto.getVaccineName());
        vaccination.setDoseNumber(dto.getDoseNumber());
        vaccination.setVaccinationDate(dto.getVaccinationDate());
        vaccination.setLocation(dto.getLocation());

        vaccinationRepository.save(vaccination);
    }

    @Override
    public Vaccination findVaccinationById(Long id) {
        Vaccination vac = vaccinationRepository.findById(id).orElse(null);
        if (vac == null) return null;

        
        HealthProfile profile = validateProfileAccess(vac.getHealthProfile().getId(), false);
        return (profile != null) ? vac : null;
    }

    @Override
    public void deleteVaccinationById(Long id) {
        Vaccination vac = vaccinationRepository.findById(id).orElse(null);
        if (vac != null) {
            
            validateProfileAccess(vac.getHealthProfile().getId(), true);
            vaccinationRepository.deleteById(id);
        }
    }

    @Override
    public long countVaccinationsByProfileId(Long profileId) {
        HealthProfile profile = validateProfileAccess(profileId, false);
        return (profile != null) ? vaccinationRepository.countByHealthProfile(profile) : 0;
    }
}