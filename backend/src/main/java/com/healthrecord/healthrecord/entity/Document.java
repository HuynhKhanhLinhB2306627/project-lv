package com.healthrecord.healthrecord.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String documentName;
    private String filePath;
    private LocalDateTime uploadDate;

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    private ExtractionStatus extractionStatus = ExtractionStatus.NONE;

    @Column(columnDefinition = "TEXT")
    private String ocrRawText;

    @Column(columnDefinition = "TEXT")
    private String extractedJson;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private Boolean isApplied = false;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private HealthProfile healthProfile;

    public enum DocumentType {
        LAB_TEST,      // Phiếu xét nghiệm (máu, nước tiểu, sinh hóa...)
        PRESCRIPTION,  // Đơn thuốc, toa thuốc
        MEDICAL_EXAM,  // Giấy khám bệnh, tóm tắt bệnh án, giấy ra viện
        VACCINATION,   // Giấy chứng nhận / sổ tiêm chủng
        INSURANCE,     // Thẻ bảo hiểm y tế
        OTHER          // Khác
    }

    public enum ExtractionStatus {
        NONE,          // Chưa quét OCR
        PROCESSING,    // Đang xử lý OCR & trích xuất
        COMPLETED,     // Trích xuất thành công
        FAILED         // Lỗi trong quá trình trích xuất
    }

    @PrePersist
    protected void onUpload() {
        this.uploadDate = LocalDateTime.now();
        if (this.extractionStatus == null) {
            this.extractionStatus = ExtractionStatus.NONE;
        }
        if (this.isApplied == null) {
            this.isApplied = false;
        }
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public HealthProfile getHealthProfile() { return healthProfile; }
    public void setHealthProfile(HealthProfile healthProfile) { this.healthProfile = healthProfile; }

    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }

    public ExtractionStatus getExtractionStatus() { return extractionStatus; }
    public void setExtractionStatus(ExtractionStatus extractionStatus) { this.extractionStatus = extractionStatus; }

    public String getOcrRawText() { return ocrRawText; }
    public void setOcrRawText(String ocrRawText) { this.ocrRawText = ocrRawText; }

    public String getExtractedJson() { return extractedJson; }
    public void setExtractedJson(String extractedJson) { this.extractedJson = extractedJson; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Boolean getIsApplied() { return isApplied; }
    public void setIsApplied(Boolean isApplied) { this.isApplied = isApplied; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}