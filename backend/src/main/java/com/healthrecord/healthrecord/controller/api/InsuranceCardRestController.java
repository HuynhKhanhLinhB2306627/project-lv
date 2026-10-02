package com.healthrecord.healthrecord.controller.api;

import com.healthrecord.healthrecord.dto.InsuranceCardDto;
import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.InsuranceCard;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.service.HealthProfileService;
import com.healthrecord.healthrecord.service.InsuranceCardService;
import com.healthrecord.healthrecord.service.ProfileAccessService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/insurance")
public class InsuranceCardRestController {

    @Autowired private InsuranceCardService insuranceCardService;
    @Autowired private HealthProfileService healthProfileService;
    @Autowired private ProfileAccessService profileAccessService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCards(@RequestParam(required = false) Long profileId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(401).build();

        List<HealthProfile> profiles = healthProfileService.getAllAccessibleProfiles();
        
        Long currentProfileId = profileId;
        if (currentProfileId == null && !profiles.isEmpty()) {
            currentProfileId = profiles.get(0).getId();
        }

        List<InsuranceCard> cards = Collections.emptyList();
        boolean canEdit = false;

        if (currentProfileId != null) {
            cards = insuranceCardService.findCardsByProfileId(currentProfileId);
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
        response.put("cards", cards.stream().map(this::convertToMap).collect(Collectors.toList()));
        response.put("canEdit", canEdit);
        response.put("profiles", profileList);
        response.put("currentProfileId", currentProfileId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCardById(@PathVariable Long id) {
        InsuranceCard card = insuranceCardService.findCardById(id);
        if (card == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convertToMap(card));
    }

    @PostMapping
    public ResponseEntity<?> saveCard(@RequestBody InsuranceCardDto cardDto) {
        try {
            insuranceCardService.saveCard(cardDto);
            return ResponseEntity.ok(Map.of("success", true, "message", "Lưu thẻ BHYT thành công"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCard(@PathVariable Long id) {
        try {
            insuranceCardService.deleteCardById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa thẻ BHYT thành công"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private Map<String, Object> convertToMap(InsuranceCard card) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", card.getId());
        map.put("profileId", card.getHealthProfile().getId());
        map.put("cardNumber", card.getCardNumber());
        map.put("registrationPlace", card.getRegistrationPlace());
        map.put("issueDate", card.getIssueDate());
        map.put("expiryDate", card.getExpiryDate());
        return map;
    }
}
