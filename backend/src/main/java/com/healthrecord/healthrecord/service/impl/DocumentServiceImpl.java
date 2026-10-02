package com.healthrecord.healthrecord.service.impl;

import com.healthrecord.healthrecord.dto.DocumentDto;
import com.healthrecord.healthrecord.entity.Document;
import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.repository.DocumentRepository;
import com.healthrecord.healthrecord.repository.HealthProfileRepository;
import com.healthrecord.healthrecord.service.DocumentService;
import com.healthrecord.healthrecord.service.ProfileAccessService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@Service
public class DocumentServiceImpl implements DocumentService {

    
    public static final String UPLOAD_DIRECTORY = "uploads";

    @Autowired private DocumentRepository documentRepository;
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
    public List<Document> findAllDocumentsByProfileId(Long profileId) {
        
        HealthProfile profile = validateProfileAccess(profileId, false);
        if (profile != null) {
            return documentRepository.findByHealthProfileOrderByUploadDateDesc(profile);
        }
        return Collections.emptyList();
    }

    @Override
    @Transactional
    public void saveDocument(DocumentDto dto) throws IOException {
        
        HealthProfile profile = validateProfileAccess(dto.getProfileId(), true);
        if (profile == null) {
            throw new IllegalArgumentException("Hồ sơ không tồn tại hoặc không có quyền truy cập");
        }

        MultipartFile file = dto.getFile();
        if (file == null || file.isEmpty()) return;

        
        
        String originalFilename = file.getOriginalFilename();
        String storedFileName = System.currentTimeMillis() + "_" + originalFilename;

        Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(storedFileName);
        Files.write(filePath, file.getBytes());

        
        Document document = new Document();
        document.setDocumentName(dto.getDocumentName());
        document.setFilePath(storedFileName); 
        document.setHealthProfile(profile);

        documentRepository.save(document);
    }

    @Override
    public Document findDocumentById(Long id) {
        Document doc = documentRepository.findById(id).orElse(null);
        if (doc == null) return null;

        
        HealthProfile profile = validateProfileAccess(doc.getHealthProfile().getId(), false);
        return (profile != null) ? doc : null;
    }

    @Override
    @Transactional
    public void deleteDocumentById(Long id) throws IOException {
        Document doc = documentRepository.findById(id).orElse(null);
        if (doc != null) {
            
            validateProfileAccess(doc.getHealthProfile().getId(), true);

            
            Path filePath = Paths.get(UPLOAD_DIRECTORY, doc.getFilePath());
            Files.deleteIfExists(filePath);

            
            documentRepository.deleteById(id);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) throws Exception {
        try {
            Path filePath = Paths.get(UPLOAD_DIRECTORY).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new Exception("File not found: " + fileName);
            }
        } catch (Exception ex) {
            throw new Exception("File not found: " + fileName, ex);
        }
    }

    @Override
    public long countDocumentsByProfileId(Long profileId) {
        HealthProfile profile = validateProfileAccess(profileId, false);
        return (profile != null) ? documentRepository.countByHealthProfile(profile) : 0;
    }
}