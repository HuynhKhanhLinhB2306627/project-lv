package com.healthrecord.healthrecord.service.impl;

import com.healthrecord.healthrecord.dto.BodyMetricDto;
import com.healthrecord.healthrecord.entity.BodyMetric;
import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.repository.BodyMetricRepository;
import com.healthrecord.healthrecord.repository.HealthProfileRepository;
import com.healthrecord.healthrecord.service.BodyMetricService;
import com.healthrecord.healthrecord.service.ProfileAccessService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class BodyMetricServiceImpl implements BodyMetricService {

    @Autowired private BodyMetricRepository bodyMetricRepository;
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
    public Optional<BodyMetric> findLatestBodyMetricByProfileId(Long profileId) {
        HealthProfile profile = validateProfileAccess(profileId, false);
        if (profile != null) {
            return bodyMetricRepository.findLatestByProfileId(profileId);
        }
        return Optional.empty();
    }

    @Override
    public List<BodyMetric> findAllBodyMetricsByProfileId(Long profileId) {
        HealthProfile profile = validateProfileAccess(profileId, false);
        if (profile != null) {
            return bodyMetricRepository.findByHealthProfileOrderByLogDateDesc(profile);
        }
        return Collections.emptyList();
    }

    @Override
    @Transactional
    public void saveOrUpdateBodyMetric(BodyMetricDto dto) {
        HealthProfile profile = validateProfileAccess(dto.getProfileId(), true);
        if (profile == null) {
            throw new IllegalArgumentException("Hồ sơ không tồn tại hoặc không có quyền truy cập");
        }

        BodyMetric metric;
        if (dto.getId() != null) {
            metric = bodyMetricRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Bản ghi không tồn tại"));

            if (!metric.getHealthProfile().getId().equals(profile.getId())) {
                throw new SecurityException("Dữ liệu không khớp với hồ sơ.");
            }
        } else {
            metric = new BodyMetric();
            metric.setHealthProfile(profile);
        }

        
        metric.setHeightInCm(dto.getHeightInCm());
        metric.setWeightInKg(dto.getWeightInKg());

        
        metric.setSystolicBp(dto.getSystolicBp());
        metric.setDiastolicBp(dto.getDiastolicBp());
        metric.setCholesterol(dto.getCholesterol() != null ? dto.getCholesterol() : 1);
        metric.setGlucose(dto.getGlucose() != null ? dto.getGlucose() : 1);
        metric.setSmoking(dto.getSmoking() != null ? dto.getSmoking() : false);
        metric.setAlcohol(dto.getAlcohol() != null ? dto.getAlcohol() : false);
        metric.setPhysicalActivity(dto.getPhysicalActivity() != null ? dto.getPhysicalActivity() : true);

        if (dto.getLogDate() != null) {
            metric.setLogDate(dto.getLogDate());
        }

        bodyMetricRepository.save(metric);
    }

    @Override
    public Optional<BodyMetric> findBodyMetricById(Long id) {
        Optional<BodyMetric> opt = bodyMetricRepository.findById(id);
        if (opt.isEmpty()) return Optional.empty();

        HealthProfile profile = validateProfileAccess(opt.get().getHealthProfile().getId(), false);
        return (profile != null) ? opt : Optional.empty();
    }

    @Override
    public void deleteBodyMetricById(Long id) {
        Optional<BodyMetric> opt = bodyMetricRepository.findById(id);
        if (opt.isPresent()) {
            validateProfileAccess(opt.get().getHealthProfile().getId(), true);
            bodyMetricRepository.deleteById(id);
        }
    }
}