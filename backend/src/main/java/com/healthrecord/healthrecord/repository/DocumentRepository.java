package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.Document;
import com.healthrecord.healthrecord.entity.HealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByHealthProfileOrderByUploadDateDesc(HealthProfile healthProfile);

    long countByHealthProfile(HealthProfile healthProfile);
    
    
    Document findByFilePath(String filePath);
}