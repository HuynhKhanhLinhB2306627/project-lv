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
    @Autowired private com.healthrecord.healthrecord.service.MedicalDocumentExtractionService medicalDocumentExtractionService;

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
                                           @RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "autoExtract", defaultValue = "false") boolean autoExtract) {
        try {
            DocumentDto dto = new DocumentDto();
            dto.setProfileId(profileId);
            dto.setDocumentName(documentName);
            dto.setFile(file);
            Document savedDoc = documentService.saveDocument(dto);

            if (savedDoc != null && autoExtract) {
                try {
                    savedDoc = medicalDocumentExtractionService.extractDocument(savedDoc.getId());
                } catch (Exception ex) {
                    System.err.println("Auto extract failed: " + ex.getMessage());
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tải lên tài liệu thành công.",
                    "document", savedDoc != null ? convertToDto(savedDoc) : null
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/extract")
    public ResponseEntity<?> extractDocument(@PathVariable Long id) {
        try {
            Document document = documentService.findDocumentById(id);
            if (document == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy tài liệu hoặc không có quyền truy cập."));
            }

            Document extracted = medicalDocumentExtractionService.extractDocument(id);
            return ResponseEntity.ok(Map.of(
                    "success", extracted.getExtractionStatus() == Document.ExtractionStatus.COMPLETED,
                    "message", extracted.getExtractionStatus() == Document.ExtractionStatus.COMPLETED ? "Trích xuất tài liệu thành công!" : "Trích xuất thất bại: " + extracted.getErrorMessage(),
                    "document", convertToDto(extracted)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/extraction-result")
    public ResponseEntity<?> getExtractionResult(@PathVariable Long id) {
        Document document = documentService.findDocumentById(id);
        if (document == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy tài liệu."));
        }
        return ResponseEntity.ok(convertToDto(document));
    }

    @PostMapping("/{id}/apply-to-metrics")
    public ResponseEntity<?> applyToMetrics(
            @PathVariable Long id, 
            @RequestParam(required = false) Long targetProfileId,
            @RequestBody(required = false) Map<String, Object> customMetrics) {
        try {
            Map<String, Object> result = medicalDocumentExtractionService.applyToBodyMetrics(id, targetProfileId, customMetrics);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/apply-to-medications")
    public ResponseEntity<?> applyToMedications(
            @PathVariable Long id, 
            @RequestParam(required = false) Long targetProfileId,
            @RequestBody(required = false) List<Map<String, Object>> customMedications) {
        try {
            Map<String, Object> result = medicalDocumentExtractionService.applyToMedications(id, targetProfileId, customMedications);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/apply-to-appointment")
    public ResponseEntity<?> applyToAppointment(
            @PathVariable Long id, 
            @RequestParam(required = false) Long targetProfileId,
            @RequestBody(required = false) Map<String, Object> customAppointment) {
        try {
            Map<String, Object> result = medicalDocumentExtractionService.applyToAppointment(id, targetProfileId, customAppointment);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/apply-auto")
    public ResponseEntity<?> applyAuto(@PathVariable Long id, @RequestParam(required = false) Long targetProfileId) {
        try {
            Document document = documentService.findDocumentById(id);
            if (document == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy tài liệu."));
            }

            if (document.getDocumentType() == Document.DocumentType.LAB_TEST) {
                return ResponseEntity.ok(medicalDocumentExtractionService.applyToBodyMetrics(id, targetProfileId));
            } else if (document.getDocumentType() == Document.DocumentType.PRESCRIPTION) {
                return ResponseEntity.ok(medicalDocumentExtractionService.applyToMedications(id, targetProfileId));
            } else if (document.getDocumentType() == Document.DocumentType.MEDICAL_EXAM) {
                return ResponseEntity.ok(medicalDocumentExtractionService.applyToAppointment(id, targetProfileId));
            } else {
                // Thử áp dụng lần lượt nếu có dữ liệu tương ứng
                try {
                    return ResponseEntity.ok(medicalDocumentExtractionService.applyToBodyMetrics(id, targetProfileId));
                } catch (Exception e1) {
                    try {
                        return ResponseEntity.ok(medicalDocumentExtractionService.applyToMedications(id, targetProfileId));
                    } catch (Exception e2) {
                        return ResponseEntity.ok(medicalDocumentExtractionService.applyToAppointment(id, targetProfileId));
                    }
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/change-profile")
    public ResponseEntity<?> changeProfile(
            @PathVariable Long id, 
            @RequestParam(required = false) Long targetProfileId,
            @RequestParam(required = false) Long newProfileId) {
        try {
            Long finalProfileId = targetProfileId != null ? targetProfileId : newProfileId;
            if (finalProfileId == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu ID hồ sơ đích (targetProfileId)."));
            }
            Document doc = medicalDocumentExtractionService.changeDocumentProfile(id, finalProfileId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã chuyển tài liệu sang hồ sơ mới thành công!",
                    "document", convertToDto(doc)
            ));
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
        dto.setDocumentType(document.getDocumentType() != null ? document.getDocumentType().name() : null);
        dto.setExtractionStatus(document.getExtractionStatus() != null ? document.getExtractionStatus().name() : null);
        dto.setSummary(document.getSummary());
        dto.setExtractedJson(document.getExtractedJson());
        dto.setIsApplied(document.getIsApplied());
        dto.setErrorMessage(document.getErrorMessage());
        dto.setOcrRawText(document.getOcrRawText());

        if (document.getHealthProfile() != null) {
            dto.setProfileId(document.getHealthProfile().getId());
            dto.setProfileName(document.getHealthProfile().getFullName());
        }
        return dto;
    }
}
