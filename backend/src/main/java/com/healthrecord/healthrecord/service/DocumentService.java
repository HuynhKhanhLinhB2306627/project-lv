package com.healthrecord.healthrecord.service;

import com.healthrecord.healthrecord.dto.DocumentDto;
import com.healthrecord.healthrecord.entity.Document;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.util.List;

public interface DocumentService {

    List<Document> findAllDocumentsByProfileId(Long profileId);

    Document saveDocument(DocumentDto documentDto) throws IOException;

    Document updateDocument(Document document);

    Document findDocumentById(Long id);

    void deleteDocumentById(Long id) throws IOException;

    Resource loadFileAsResource(String fileName) throws Exception;

    long countDocumentsByProfileId(Long profileId);
}