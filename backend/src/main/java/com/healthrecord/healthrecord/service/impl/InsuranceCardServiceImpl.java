package com.healthrecord.healthrecord.service.impl;

import com.healthrecord.healthrecord.dto.InsuranceCardDto;
import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.InsuranceCard;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.repository.HealthProfileRepository;
import com.healthrecord.healthrecord.repository.InsuranceCardRepository;
import com.healthrecord.healthrecord.service.InsuranceCardService;
import com.healthrecord.healthrecord.service.ProfileAccessService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class InsuranceCardServiceImpl implements InsuranceCardService {

    @Autowired private InsuranceCardRepository insuranceCardRepository;
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
    public List<InsuranceCard> findCardsByProfileId(Long profileId) {
        
        HealthProfile profile = validateProfileAccess(profileId, false);
        if (profile != null) {
            return insuranceCardRepository.findByHealthProfileOrderByExpiryDateDesc(profile);
        }
        return Collections.emptyList();
    }

    @Override
    public void saveCard(InsuranceCardDto dto) {
        
        HealthProfile profile = validateProfileAccess(dto.getProfileId(), true);
        if (profile == null) {
            throw new IllegalArgumentException("Hồ sơ không tồn tại hoặc không có quyền truy cập");
        }

        InsuranceCard card;
        if (dto.getId() != null) {
            card = insuranceCardRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Thẻ bảo hiểm không tồn tại"));

            
            if (!card.getHealthProfile().getId().equals(profile.getId())) {
                throw new SecurityException("Dữ liệu không khớp với hồ sơ.");
            }
        } else {
            card = new InsuranceCard();
            card.setHealthProfile(profile);
        }

        card.setCardNumber(dto.getCardNumber());
        card.setRegistrationPlace(dto.getRegistrationPlace());
        card.setIssueDate(dto.getIssueDate());
        card.setExpiryDate(dto.getExpiryDate());

        insuranceCardRepository.save(card);
    }

    @Override
    public InsuranceCard findCardById(Long id) {
        InsuranceCard card = insuranceCardRepository.findById(id).orElse(null);
        if (card == null) return null;

        
        HealthProfile profile = validateProfileAccess(card.getHealthProfile().getId(), false);
        return (profile != null) ? card : null;
    }

    @Override
    public void deleteCardById(Long id) {
        InsuranceCard card = insuranceCardRepository.findById(id).orElse(null);
        if (card != null) {
            
            validateProfileAccess(card.getHealthProfile().getId(), true);
            insuranceCardRepository.deleteById(id);
        }
    }

    @Override
    public long countCardsByProfileId(Long profileId) {
        HealthProfile profile = validateProfileAccess(profileId, false);
        if (profile != null) {
            return insuranceCardRepository.countByHealthProfile(profile);
        }
        return 0;
    }
}