package com.healthrecord.healthrecord.controller.api;

import com.healthrecord.healthrecord.entity.Document;
import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.ProfileAccess;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.repository.DocumentRepository;
import com.healthrecord.healthrecord.repository.HealthProfileRepository;
import com.healthrecord.healthrecord.repository.ProfileAccessRepository;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;












@RestController
@RequestMapping("/api/files")
public class FileAccessController {

    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private HealthProfileRepository healthProfileRepository;
    
    @Autowired
    private ProfileAccessRepository profileAccessRepository;

    private static final String UPLOAD_DIR = "uploads";

    





    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        
        Document document = documentRepository.findByFilePath("uploads/" + filename);
        if (document == null) {
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        
        HealthProfile profile = document.getHealthProfile();
        if (!hasAccessToProfile(currentUser, profile)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }

        
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            File file = filePath.toFile();
            
            if (!file.exists() || !file.canRead()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Resource resource = new FileSystemResource(file);
            
            
            String contentType = determineContentType(filename);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    






    private boolean hasAccessToProfile(User user, HealthProfile profile) {
        if (profile == null) {
            return false;
        }

        
        if (profile.getUser().getId().equals(user.getId())) {
            return true;
        }

        
        Optional<ProfileAccess> access = profileAccessRepository
                .findByHealthProfileIdAndUserId(profile.getId(), user.getId());
        
        
        return access.isPresent() && access.get().getStatus() == ProfileAccess.AccessStatus.ACTIVE;
    }

    


    private String determineContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        return "application/octet-stream";
    }
}
