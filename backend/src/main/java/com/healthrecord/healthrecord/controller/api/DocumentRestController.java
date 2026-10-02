package com.healthrecord.healthrecord.controller.api;

import com.healthrecord.healthrecord.dto.DocumentDto;
import com.healthrecord.healthrecord.dto.DocumentResponseDto;
import com.healthrecord.healthrecord.entity.Document;
import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.service.DocumentService;
import com.healthrecord.healthrecord.service.HealthProfileService;
import com.healthrecord.healthrecord.service.ProfileAccessService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentRestController {

    @Autowired private DocumentService documentService;
    @Autowired private HealthProfileService healthProfileService;
    @Autowired private ProfileAccessService profileAccessService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getDocuments(@RequestParam(required = false) Long profileId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(401).build();

        List<HealthProfile> profiles = healthProfileService.getAllAccessibleProfiles();
        Long currentProfileId = profileId;
        if (currentProfileId == null && !profiles.isEmpty()) {
            currentProfileId = profiles.get(0).getId();
        }

        List<Document> documents = Collections.emptyList();
        boolean canEdit = false;

        if (currentProfileId != null) {
            documents = documentService.findAllDocumentsByProfileId(currentProfileId);
            HealthProfile profile = healthProfileService.findProfileById(currentProfileId);
            if (profile != null) {
                boolean isOwner = profile.getUser().getId().equals(currentUser.getId());
                boolean isEditor = profileAccessService.hasEditorAccess(currentUser.getId(), currentProfileId);
                canEdit = isOwner || isEditor;
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("documents", documents.stream().map(this::convertToDto).collect(Collectors.toList()));
        response.put("canEdit", canEdit);
        response.put("profiles", profiles.stream().map(p -> Map.of("id", p.getId(), "profileName", p.getProfileName(), "fullName", p.getFullName())).collect(Collectors.toList()));
        response.put("currentProfileId", currentProfileId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("profileId") Long profileId,
                                           @RequestParam("documentName") String documentName,
                                           @RequestParam("file") MultipartFile file) {
        try {
            DocumentDto dto = new DocumentDto();
            dto.setProfileId(profileId);
            dto.setDocumentName(documentName);
            dto.setFile(file);
            documentService.saveDocument(dto);
            return ResponseEntity.ok(Map.of("success", true, "message", "Tải lên tài liệu thành công."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocumentById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa tài liệu thành công."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private DocumentResponseDto convertToDto(Document document) {
        DocumentResponseDto dto = new DocumentResponseDto();
        dto.setId(document.getId());
        dto.setDocumentName(document.getDocumentName());
        dto.setFilePath(document.getFilePath());
        dto.setUploadDate(document.getUploadDate());
        return dto;
    }
}
