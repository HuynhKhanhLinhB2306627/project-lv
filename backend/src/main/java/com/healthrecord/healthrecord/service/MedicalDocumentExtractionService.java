package com.healthrecord.healthrecord.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthrecord.healthrecord.entity.Document;
import com.healthrecord.healthrecord.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class MedicalDocumentExtractionService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private BodyMetricService bodyMetricService;

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private com.healthrecord.healthrecord.repository.MedicineRepository medicineRepository;

    @Autowired
    private com.healthrecord.healthrecord.repository.HealthProfileRepository healthProfileRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static final String UPLOAD_DIRECTORY = "uploads";

    /**
     * Kích hoạt trích xuất thông tin OCR & Phân tích y tế từ tài liệu
     */
    @Transactional
    public Document extractDocument(Long documentId) throws Exception {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu với id: " + documentId));

        if (apiKey == null || apiKey.trim().isEmpty()) {
            document.setExtractionStatus(Document.ExtractionStatus.FAILED);
            document.setErrorMessage("Chưa cấu hình GEMINI_API_KEY.");
            return documentRepository.save(document);
        }

        Path filePath = Paths.get(UPLOAD_DIRECTORY, document.getFilePath());
        if (!Files.exists(filePath)) {
            document.setExtractionStatus(Document.ExtractionStatus.FAILED);
            document.setErrorMessage("Tệp tin tài liệu không tồn tại trên hệ thống lưu trữ.");
            return documentRepository.save(document);
        }

        String mimeType = determineMimeType(document.getFilePath());
        if (mimeType == null) {
            document.setExtractionStatus(Document.ExtractionStatus.FAILED);
            document.setErrorMessage("Định dạng tệp không được hỗ trợ để nhận diện OCR (chỉ hỗ trợ PNG, JPG, JPEG, WEBP, PDF).");
            return documentRepository.save(document);
        }

        document.setExtractionStatus(Document.ExtractionStatus.PROCESSING);
        document.setErrorMessage(null);
        document = documentRepository.save(document);

        try {
            byte[] fileBytes = Files.readAllBytes(filePath);
            String base64Data = Base64.getEncoder().encodeToString(fileBytes);

            String prompt = buildMedicalExtractionPrompt();
            String rawResponse = callGeminiVision(prompt, base64Data, mimeType);

            String cleanedJson = cleanupModelText(rawResponse);
            JsonNode rootNode = objectMapper.readTree(cleanedJson);

            // Phân loại tài liệu
            String docTypeStr = rootNode.path("documentType").asText("OTHER");
            Document.DocumentType docType;
            try {
                docType = Document.DocumentType.valueOf(docTypeStr.toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                docType = Document.DocumentType.OTHER;
            }

            String summary = rootNode.path("summary").asText(null);
            String ocrRawText = rootNode.path("ocrRawText").asText(null);

            document.setDocumentType(docType);
            document.setSummary(summary);
            document.setOcrRawText(ocrRawText);
            document.setExtractedJson(cleanedJson);
            document.setExtractionStatus(Document.ExtractionStatus.COMPLETED);
            document.setErrorMessage(null);

            return documentRepository.save(document);

        } catch (Exception ex) {
            ex.printStackTrace();
            document.setExtractionStatus(Document.ExtractionStatus.FAILED);
            document.setErrorMessage("Lỗi trong quá trình AI phân tích tài liệu: " + ex.getMessage());
            return documentRepository.save(document);
        }
    }

    private String determineMimeType(String fileName) {
        if (fileName == null) return null;
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".pdf")) return "application/pdf";
        return null;
    }

    private String callGeminiVision(String prompt, String base64Data, String mimeType) throws Exception {
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> inlineData = new HashMap<>();
        inlineData.put("mime_type", mimeType);
        inlineData.put("data", base64Data);

        Map<String, Object> filePart = new HashMap<>();
        filePart.put("inline_data", inlineData);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart, filePart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", Collections.singletonList(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        List<String> candidateUrls = getCandidateUrls();
        Exception lastException = null;

        for (String targetUrl : candidateUrls) {
            String fullUrl = targetUrl.endsWith("key=") ? (targetUrl + apiKey) : (targetUrl + "?key=" + apiKey);
            int maxRetries = 2;

            for (int attempt = 0; attempt <= maxRetries; attempt++) {
                try {
                    ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, requestEntity, String.class);

                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode candidates = root.path("candidates");
                    if (!candidates.isArray() || candidates.isEmpty()) {
                        throw new IllegalStateException("Gemini không trả về kết quả phân tích hợp lệ.");
                    }

                    JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
                    if (textNode.isMissingNode() || textNode.asText().trim().isEmpty()) {
                        throw new IllegalStateException("Gemini trả về nội dung rỗng.");
                    }

                    return textNode.asText();

                } catch (Exception e) {
                    lastException = e;
                    String errMsg = e.getMessage() != null ? e.getMessage().toLowerCase(Locale.ROOT) : "";

                    // Nếu gặp 503 (quá tải), retry hoặc chuyển model dự phòng
                    if (errMsg.contains("503") || errMsg.contains("unavailable") || errMsg.contains("high demand") || errMsg.contains("resource_exhausted")) {
                        System.err.println("Gemini model tại [" + targetUrl + "] đang quá tải (503). Đang xử lý fallback...");
                        if (attempt < maxRetries) {
                            try {
                                Thread.sleep(1500 * (attempt + 1));
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                            continue;
                        }
                        // Hết lượt retry của model này -> break để vòng lặp ngoài chuyển sang model tiếp theo
                        break;
                    }

                    // Nếu lỗi khác (ví dụ 400 Bad Request), throw luôn
                    throw e;
                }
            }
        }

        throw lastException != null ? lastException : new Exception("Không thể kết nối đến dịch vụ AI sau khi thử các model dự phòng.");
    }

    private List<String> getCandidateUrls() {
        List<String> urls = new ArrayList<>();
        if (apiUrl != null && !apiUrl.isBlank()) {
            urls.add(apiUrl.trim());
        }

        // Các model dự phòng cực kỳ ổn định của Gemini
        String baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/";
        String suffix = ":generateContent?key=";
        List<String> fallbackModels = List.of("gemini-2.0-flash", "gemini-1.5-flash", "gemini-2.5-flash");

        for (String m : fallbackModels) {
            String candidate = baseUrl + m + suffix;
            if (!urls.contains(candidate)) {
                urls.add(candidate);
            }
        }
        return urls;
    }

    private String buildMedicalExtractionPrompt() {
        return "Bạn là trợ lý AI y tế chuyên nghiệp có khả năng đọc OCR và trích xuất thông tin có cấu trúc từ tài liệu y tế (phiếu xét nghiệm, đơn thuốc, phiếu khám, giấy ra viện, sổ tiêm chủng, thẻ bảo hiểm y tế).\n"
                + "Hãy phân tích hình ảnh/tài liệu được cung cấp và trích xuất dữ liệu chính xác sang định dạng JSON.\n"
                + "YÊU CẦU QUAN TRỌNG:\n"
                + "1. Chỉ trả về DUY NHẤT một chuỗi JSON hợp lệ. Không viết lời mở đầu, không viết lời kết, không bọc trong ```json ``` nếu có thể.\n"
                + "2. 'documentType' PHẢI là một trong các giá trị: 'LAB_TEST' (nếu là kết quả xét nghiệm máu/nước tiểu/sinh hóa), 'PRESCRIPTION' (nếu là toa thuốc/đơn thuốc), 'MEDICAL_EXAM' (nếu là phiếu khám bệnh, giấy ra viện, tóm tắt bệnh án), 'VACCINATION' (nếu là giấy tiêm chủng), 'INSURANCE' (nếu là thẻ BHYT), hoặc 'OTHER'.\n"
                + "3. Đối với các chỉ số sinh hóa tim mạch / xét nghiệm:\n"
                + "   - glucose: mức phân loại (1: bình thường [< 6.1 mmol/L hoặc < 110 mg/dL], 2: trên bình thường [6.1 - 6.9 mmol/L], 3: cao [>= 7.0 mmol/L]).\n"
                + "   - cholesterol: mức phân loại (1: bình thường [< 5.2 mmol/L hoặc < 200 mg/dL], 2: trên bình thường [5.2 - 6.2 mmol/L], 3: cao [>= 6.2 mmol/L]).\n"
                + "4. Nếu trường nào không có thông tin trong tài liệu, hãy để giá trị null.\n"
                + "\nSCHEMA JSON BẮT BUỘC:\n"
                + "{\n"
                + "  \"documentType\": \"LAB_TEST | PRESCRIPTION | MEDICAL_EXAM | VACCINATION | INSURANCE | OTHER\",\n"
                + "  \"documentDate\": \"YYYY-MM-DD hoặc null\",\n"
                + "  \"facilityName\": \"Tên bệnh viện hoặc phòng khám hoặc null\",\n"
                + "  \"doctorName\": \"Tên bác sĩ hoặc null\",\n"
                + "  \"patientName\": \"Tên bệnh nhân trên tài liệu hoặc null\",\n"
                + "  \"summary\": \"Tóm tắt 1-2 câu ngắn gọn về tài liệu này bằng tiếng Việt\",\n"
                + "  \"ocrRawText\": \"Toàn bộ nội dung văn bản đọc được từ tài liệu\",\n"
                + "  \"metrics\": {\n"
                + "    \"heightInCm\": 170.0,\n"
                + "    \"weightInKg\": 65.0,\n"
                + "    \"systolicBp\": 120,\n"
                + "    \"diastolicBp\": 80,\n"
                + "    \"glucose\": 1,\n"
                + "    \"glucoseValue\": 5.2,\n"
                + "    \"cholesterol\": 1,\n"
                + "    \"cholesterolValue\": 4.5,\n"
                + "    \"smoking\": null,\n"
                + "    \"alcohol\": null,\n"
                + "    \"physicalActivity\": null,\n"
                + "    \"otherLabResults\": [\n"
                + "      { \"name\": \"Tên xét nghiệm\", \"value\": \"Giá trị đo\", \"unit\": \"Đơn vị\", \"normalRange\": \"Khoảng tham chiếu\", \"status\": \"Bình thường | Cao | Thấp\" }\n"
                + "    ]\n"
                + "  },\n"
                + "  \"medications\": [\n"
                + "    {\n"
                + "      \"medicineName\": \"Tên thuốc\",\n"
                + "      \"dosage\": \"Hàm lượng (ví dụ 500mg, 10ml)\",\n"
                + "      \"quantity\": 30,\n"
                + "      \"unit\": \"viên / gói / chai\",\n"
                + "      \"durationInDays\": 10,\n"
                + "      \"morningDose\": \"1\",\n"
                + "      \"noonDose\": \"0\",\n"
                + "      \"afternoonDose\": \"0\",\n"
                + "      \"eveningDose\": \"1\",\n"
                + "      \"timing\": \"Uống sau ăn\",\n"
                + "      \"instructions\": \"Cách dùng cụ thể\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"appointment\": {\n"
                + "    \"appointmentDate\": \"YYYY-MM-DD\",\n"
                + "    \"doctorName\": \"Tên bác sĩ\",\n"
                + "    \"location\": \"Nơi khám\",\n"
                + "    \"reason\": \"Lý do khám\",\n"
                + "    \"diagnosis\": \"Chẩn đoán bệnh\",\n"
                + "    \"doctorNote\": \"Lời dặn hoặc chỉ định điều trị\"\n"
                + "  },\n"
                + "  \"vaccination\": {\n"
                + "    \"vaccineName\": \"Tên vaccine\",\n"
                + "    \"doseNumber\": 1,\n"
                + "    \"vaccinationDate\": \"YYYY-MM-DD\",\n"
                + "    \"location\": \"Nơi tiêm\"\n"
                + "  },\n"
                + "  \"insuranceCard\": {\n"
                + "    \"cardNumber\": \"Mã thẻ BHYT\",\n"
                + "    \"fullName\": \"Họ tên chủ thẻ\",\n"
                + "    \"facilityCode\": \"Mã nơi KCB ban đầu\",\n"
                + "    \"facilityName\": \"Tên nơi KCB ban đầu\",\n"
                + "    \"expiryDate\": \"YYYY-MM-DD\"\n"
                + "  }\n"
                + "}";
    }

    public static String cleanupModelText(String text) {
        if (text == null) return "{}";
        return text.trim()
                .replaceAll("^```json\\s*", "")
                .replaceAll("^```\\s*", "")
                .replaceAll("\\s*```$", "")
                .trim();
    }

    /**
     * Chuyển tài liệu sang hồ sơ sức khỏe khác
     */
    @Transactional
    public Document changeDocumentProfile(Long documentId, Long newProfileId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu với ID: " + documentId));

        com.healthrecord.healthrecord.entity.HealthProfile newProfile = healthProfileRepository.findById(newProfileId)
                .orElseThrow(() -> new IllegalArgumentException("Hồ sơ sức khỏe mới không tồn tại với ID: " + newProfileId));

        document.setHealthProfile(newProfile);
        return documentRepository.save(document);
    }

    /**
     * Tự động áp dụng dữ liệu trích xuất vào module Chỉ số cơ thể (BodyMetrics)
     */
    @Transactional
    public Map<String, Object> applyToBodyMetrics(Long documentId, Long targetProfileId) throws Exception {
        return applyToBodyMetrics(documentId, targetProfileId, null);
    }

    @Transactional
    public Map<String, Object> applyToBodyMetrics(Long documentId, Long targetProfileId, Map<String, Object> customMetrics) throws Exception {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu với ID: " + documentId));

        if (document.getExtractedJson() == null || document.getExtractedJson().isBlank()) {
            throw new IllegalStateException("Tài liệu chưa có dữ liệu trích xuất để áp dụng.");
        }

        JsonNode root = objectMapper.readTree(document.getExtractedJson());
        JsonNode metricsNode = root.path("metrics");

        Long profileId = targetProfileId != null ? targetProfileId : document.getHealthProfile().getId();
        if (targetProfileId != null && !targetProfileId.equals(document.getHealthProfile().getId())) {
            changeDocumentProfile(documentId, targetProfileId);
        }

        // Lấy chỉ số gần nhất để fallback các trường bắt buộc như chiều cao/cân nặng nếu phiếu xét nghiệm không có
        Optional<com.healthrecord.healthrecord.entity.BodyMetric> latestOpt = bodyMetricService.findLatestBodyMetricByProfileId(profileId);

        Double height = null;
        Double weight = null;
        Integer systolic = null;
        Integer diastolic = null;
        Integer glucose = null;
        Integer cholesterol = null;
        Boolean smoking = null;
        Boolean alcohol = null;
        Boolean physicalActivity = null;

        if (customMetrics != null && !customMetrics.isEmpty()) {
            height = parseDouble(customMetrics.get("heightInCm"));
            weight = parseDouble(customMetrics.get("weightInKg"));
            systolic = parseInteger(customMetrics.get("systolicBp"));
            diastolic = parseInteger(customMetrics.get("diastolicBp"));
            glucose = parseInteger(customMetrics.get("glucose"));
            cholesterol = parseInteger(customMetrics.get("cholesterol"));
            smoking = parseBoolean(customMetrics.get("smoking"));
            alcohol = parseBoolean(customMetrics.get("alcohol"));
            physicalActivity = parseBoolean(customMetrics.get("physicalActivity"));
        }

        if (height == null && metricsNode.hasNonNull("heightInCm")) {
            height = metricsNode.get("heightInCm").asDouble();
        }
        if (height == null) {
            height = latestOpt.map(com.healthrecord.healthrecord.entity.BodyMetric::getHeightInCm).orElse(170.0);
        }

        if (weight == null && metricsNode.hasNonNull("weightInKg")) {
            weight = metricsNode.get("weightInKg").asDouble();
        }
        if (weight == null) {
            weight = latestOpt.map(com.healthrecord.healthrecord.entity.BodyMetric::getWeightInKg).orElse(65.0);
        }

        if (systolic == null && metricsNode.hasNonNull("systolicBp")) {
            systolic = metricsNode.get("systolicBp").asInt();
        } else if (systolic == null && latestOpt.isPresent()) {
            systolic = latestOpt.get().getSystolicBp();
        }

        if (diastolic == null && metricsNode.hasNonNull("diastolicBp")) {
            diastolic = metricsNode.get("diastolicBp").asInt();
        } else if (diastolic == null && latestOpt.isPresent()) {
            diastolic = latestOpt.get().getDiastolicBp();
        }

        if (glucose == null && metricsNode.hasNonNull("glucose")) {
            int g = metricsNode.get("glucose").asInt();
            glucose = (g >= 1 && g <= 3) ? g : 1;
        }

        if (cholesterol == null && metricsNode.hasNonNull("cholesterol")) {
            int c = metricsNode.get("cholesterol").asInt();
            cholesterol = (c >= 1 && c <= 3) ? c : 1;
        }

        if (smoking == null && metricsNode.hasNonNull("smoking")) {
            smoking = metricsNode.get("smoking").asBoolean();
        }
        if (alcohol == null && metricsNode.hasNonNull("alcohol")) {
            alcohol = metricsNode.get("alcohol").asBoolean();
        }
        if (physicalActivity == null && metricsNode.hasNonNull("physicalActivity")) {
            physicalActivity = metricsNode.get("physicalActivity").asBoolean();
        }

        // Đảm bảo tính hợp lệ của huyết áp
        if (systolic != null && diastolic != null && systolic <= diastolic) {
            systolic = null;
            diastolic = null;
        }

        com.healthrecord.healthrecord.dto.BodyMetricDto dto = new com.healthrecord.healthrecord.dto.BodyMetricDto();
        dto.setProfileId(profileId);
        dto.setHeightInCm(height);
        dto.setWeightInKg(weight);
        dto.setSystolicBp(systolic);
        dto.setDiastolicBp(diastolic);
        dto.setGlucose(glucose != null ? glucose : 1);
        dto.setCholesterol(cholesterol != null ? cholesterol : 1);
        dto.setSmoking(smoking);
        dto.setAlcohol(alcohol);
        dto.setPhysicalActivity(physicalActivity);

        // Ngày đo
        String docDate = (customMetrics != null && customMetrics.containsKey("logDate") && customMetrics.get("logDate") != null) 
                ? customMetrics.get("logDate").toString() 
                : root.path("documentDate").asText(null);
        if (docDate != null && !docDate.isBlank()) {
            try {
                java.time.LocalDate date = java.time.LocalDate.parse(docDate.trim().substring(0, Math.min(docDate.trim().length(), 10)));
                dto.setLogDate(date.atTime(java.time.LocalTime.now()));
            } catch (Exception e) {
                dto.setLogDate(java.time.LocalDateTime.now());
            }
        } else {
            dto.setLogDate(java.time.LocalDateTime.now());
        }

        bodyMetricService.saveOrUpdateBodyMetric(dto);

        // Đồng bộ dữ liệu người dùng đã hiệu chỉnh vào document.extractedJson
        if (customMetrics != null && !customMetrics.isEmpty() && root.isObject()) {
            try {
                com.fasterxml.jackson.databind.node.ObjectNode rootObj = (com.fasterxml.jackson.databind.node.ObjectNode) root;
                com.fasterxml.jackson.databind.node.ObjectNode metricsObj = rootObj.has("metrics") && rootObj.get("metrics").isObject()
                        ? (com.fasterxml.jackson.databind.node.ObjectNode) rootObj.get("metrics")
                        : rootObj.putObject("metrics");

                if (dto.getHeightInCm() != null) metricsObj.put("heightInCm", dto.getHeightInCm());
                if (dto.getWeightInKg() != null) metricsObj.put("weightInKg", dto.getWeightInKg());
                if (dto.getSystolicBp() != null) metricsObj.put("systolicBp", dto.getSystolicBp());
                if (dto.getDiastolicBp() != null) metricsObj.put("diastolicBp", dto.getDiastolicBp());
                if (dto.getGlucose() != null) metricsObj.put("glucose", dto.getGlucose());
                if (dto.getCholesterol() != null) metricsObj.put("cholesterol", dto.getCholesterol());
                if (customMetrics.containsKey("glucoseValue") && customMetrics.get("glucoseValue") != null) {
                    metricsObj.put("glucoseValue", customMetrics.get("glucoseValue").toString());
                }
                if (customMetrics.containsKey("cholesterolValue") && customMetrics.get("cholesterolValue") != null) {
                    metricsObj.put("cholesterolValue", customMetrics.get("cholesterolValue").toString());
                }
                document.setExtractedJson(objectMapper.writeValueAsString(rootObj));
            } catch (Exception ignored) {}
        }

        document.setIsApplied(true);
        documentRepository.save(document);

        return Map.of(
                "success", true,
                "message", "Đã lưu chỉ số cơ thể thành công từ tài liệu!",
                "appliedData", dto
        );
    }

    /**
     * Tự động tạo Đơn thuốc & Liệu trình từ tài liệu trích xuất (hoặc từ danh sách thuốc đã chỉnh sửa)
     */
    @Transactional
    public Map<String, Object> applyToMedications(Long documentId, Long targetProfileId) throws Exception {
        return applyToMedications(documentId, targetProfileId, null);
    }

    @Transactional
    public Map<String, Object> applyToMedications(Long documentId, Long targetProfileId, List<Map<String, Object>> customMedications) throws Exception {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu với ID: " + documentId));

        if (document.getExtractedJson() == null || document.getExtractedJson().isBlank()) {
            throw new IllegalStateException("Tài liệu chưa có dữ liệu trích xuất để áp dụng.");
        }

        JsonNode root = objectMapper.readTree(document.getExtractedJson());

        Long profileId = targetProfileId != null ? targetProfileId : document.getHealthProfile().getId();
        if (targetProfileId != null && !targetProfileId.equals(document.getHealthProfile().getId())) {
            changeDocumentProfile(documentId, targetProfileId);
        }

        java.time.LocalDate startDate = java.time.LocalDate.now();
        String docDate = root.path("documentDate").asText(null);
        if (docDate != null && !docDate.isBlank()) {
            try {
                startDate = java.time.LocalDate.parse(docDate.trim().substring(0, Math.min(docDate.trim().length(), 10)));
            } catch (Exception ignored) {}
        }

        int addedCount = 0;

        if (customMedications != null && !customMedications.isEmpty()) {
            // Sử dụng danh sách thuốc do người dùng chỉnh sửa/xác nhận
            for (Map<String, Object> med : customMedications) {
                Object nameObj = med.get("medicineName");
                if (nameObj == null || nameObj.toString().isBlank()) continue;
                String medName = nameObj.toString().trim();

                String unit = med.get("unit") != null ? med.get("unit").toString().trim() : "viên";
                String dosage = med.get("dosage") != null ? med.get("dosage").toString().trim() : "Theo đơn";
                int duration = med.get("durationInDays") != null ? parseInteger(med.get("durationInDays")) : 7;
                if (duration <= 0) duration = 7;

                // Tìm hoặc tạo Medicine
                com.healthrecord.healthrecord.entity.Medicine medicine = medicineRepository.findByNameIgnoreCase(medName).orElse(null);
                if (medicine == null) {
                    medicine = new com.healthrecord.healthrecord.entity.Medicine();
                    medicine.setName(medName);
                    medicine.setUnit(unit);
                    medicine.setDescription("Thuốc trích xuất từ đơn: " + document.getDocumentName());
                    medicine.setApproved(true);
                    medicine = medicineRepository.save(medicine);
                }

                String morning = med.get("morningDose") != null ? med.get("morningDose").toString().trim() : "0";
                String noon = med.get("noonDose") != null ? med.get("noonDose").toString().trim() : "0";
                String afternoon = med.get("afternoonDose") != null ? med.get("afternoonDose").toString().trim() : "0";
                String evening = med.get("eveningDose") != null ? med.get("eveningDose").toString().trim() : "0";

                List<java.time.LocalTime> reminderTimes = new ArrayList<>();
                List<String> doses = new ArrayList<>();
                if (!"0".equals(morning) && !morning.isBlank()) {
                    reminderTimes.add(java.time.LocalTime.of(8, 0));
                    doses.add("Sáng " + morning);
                }
                if (!"0".equals(noon) && !noon.isBlank()) {
                    reminderTimes.add(java.time.LocalTime.of(12, 0));
                    doses.add("Trưa " + noon);
                }
                if (!"0".equals(afternoon) && !afternoon.isBlank()) {
                    reminderTimes.add(java.time.LocalTime.of(16, 0));
                    doses.add("Chiều " + afternoon);
                }
                if (!"0".equals(evening) && !evening.isBlank()) {
                    reminderTimes.add(java.time.LocalTime.of(20, 0));
                    doses.add("Tối " + evening);
                }

                if (reminderTimes.isEmpty()) {
                    reminderTimes.add(java.time.LocalTime.of(8, 0));
                }

                com.healthrecord.healthrecord.dto.MedicationCourseDto courseDto = new com.healthrecord.healthrecord.dto.MedicationCourseDto();
                courseDto.setProfileId(profileId);
                courseDto.setMedicineId(medicine.getId());
                courseDto.setDosage(dosage + (doses.isEmpty() ? "" : " (" + String.join(", ", doses) + ")"));
                courseDto.setFrequency(reminderTimes.size() + " lần/ngày");
                courseDto.setTiming(med.get("timing") != null ? med.get("timing").toString() : "Uống sau ăn");
                courseDto.setNotes(med.get("instructions") != null ? med.get("instructions").toString() : null);
                courseDto.setStartDate(startDate);
                courseDto.setDurationInDays(duration);
                courseDto.setReminderTimes(reminderTimes);

                medicationService.saveMedicationCourse(courseDto);
                addedCount++;
            }

            // Đồng bộ lại vào extractedJson
            try {
                if (root.isObject()) {
                    com.fasterxml.jackson.databind.node.ObjectNode rootObj = (com.fasterxml.jackson.databind.node.ObjectNode) root;
                    rootObj.set("medications", objectMapper.valueToTree(customMedications));
                    document.setExtractedJson(objectMapper.writeValueAsString(rootObj));
                }
            } catch (Exception ignored) {}

        } else {
            // Lấy từ JSON ban đầu nếu không có custom data
            JsonNode medsNode = root.path("medications");
            if (!medsNode.isArray() || medsNode.isEmpty()) {
                throw new IllegalStateException("Không tìm thấy danh sách thuốc trong tài liệu.");
            }

            for (JsonNode med : medsNode) {
                String medName = med.path("medicineName").asText(null);
                if (medName == null || medName.isBlank()) continue;
                medName = medName.trim();

                String unit = med.path("unit").asText("viên");
                String dosage = med.path("dosage").asText("Theo đơn");
                int duration = med.path("durationInDays").asInt(7);
                if (duration <= 0) duration = 7;

                // Tìm hoặc tạo Medicine
                com.healthrecord.healthrecord.entity.Medicine medicine = medicineRepository.findByNameIgnoreCase(medName).orElse(null);
                if (medicine == null) {
                    medicine = new com.healthrecord.healthrecord.entity.Medicine();
                    medicine.setName(medName);
                    medicine.setUnit(unit);
                    medicine.setDescription("Thuốc trích xuất từ đơn: " + document.getDocumentName());
                    medicine.setApproved(true);
                    medicine = medicineRepository.save(medicine);
                }

                // Tính tần suất uống và thời gian nhắc
                String morning = med.path("morningDose").asText("0");
                String noon = med.path("noonDose").asText("0");
                String afternoon = med.path("afternoonDose").asText("0");
                String evening = med.path("eveningDose").asText("0");

                List<java.time.LocalTime> reminderTimes = new ArrayList<>();
                List<String> doses = new ArrayList<>();
                if (!"0".equals(morning) && !morning.isBlank()) {
                    reminderTimes.add(java.time.LocalTime.of(8, 0));
                    doses.add("Sáng " + morning);
                }
                if (!"0".equals(noon) && !noon.isBlank()) {
                    reminderTimes.add(java.time.LocalTime.of(12, 0));
                    doses.add("Trưa " + noon);
                }
                if (!"0".equals(afternoon) && !afternoon.isBlank()) {
                    reminderTimes.add(java.time.LocalTime.of(16, 0));
                    doses.add("Chiều " + afternoon);
                }
                if (!"0".equals(evening) && !evening.isBlank()) {
                    reminderTimes.add(java.time.LocalTime.of(20, 0));
                    doses.add("Tối " + evening);
                }

                if (reminderTimes.isEmpty()) {
                    reminderTimes.add(java.time.LocalTime.of(8, 0));
                }

                com.healthrecord.healthrecord.dto.MedicationCourseDto courseDto = new com.healthrecord.healthrecord.dto.MedicationCourseDto();
                courseDto.setProfileId(profileId);
                courseDto.setMedicineId(medicine.getId());
                courseDto.setDosage(dosage + (doses.isEmpty() ? "" : " (" + String.join(", ", doses) + ")"));
                courseDto.setFrequency(reminderTimes.size() + " lần/ngày");
                courseDto.setTiming(med.path("timing").asText("Uống sau ăn"));
                courseDto.setNotes(med.path("instructions").asText(null));
                courseDto.setStartDate(startDate);
                courseDto.setDurationInDays(duration);
                courseDto.setReminderTimes(reminderTimes);

                medicationService.saveMedicationCourse(courseDto);
                addedCount++;
            }
        }

        document.setIsApplied(true);
        documentRepository.save(document);

        return Map.of(
                "success", true,
                "message", "Đã tạo thành công " + addedCount + " liệu trình thuốc từ đơn thuốc!",
                "coursesCount", addedCount
        );
    }

    /**
     * Tự động tạo Lịch khám từ tài liệu trích xuất (hoặc từ form người dùng đã chỉnh sửa)
     */
    @Transactional
    public Map<String, Object> applyToAppointment(Long documentId, Long targetProfileId) throws Exception {
        return applyToAppointment(documentId, targetProfileId, null);
    }

    @Transactional
    public Map<String, Object> applyToAppointment(Long documentId, Long targetProfileId, Map<String, Object> customAppointment) throws Exception {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu với ID: " + documentId));

        if (document.getExtractedJson() == null || document.getExtractedJson().isBlank()) {
            throw new IllegalStateException("Tài liệu chưa có dữ liệu trích xuất để áp dụng.");
        }

        JsonNode root = objectMapper.readTree(document.getExtractedJson());
        JsonNode appNode = root.path("appointment");

        Long profileId = targetProfileId != null ? targetProfileId : document.getHealthProfile().getId();
        if (targetProfileId != null && !targetProfileId.equals(document.getHealthProfile().getId())) {
            changeDocumentProfile(documentId, targetProfileId);
        }

        java.time.LocalDate appDate = java.time.LocalDate.now();
        String dateStr = (customAppointment != null && customAppointment.get("appointmentDate") != null)
                ? customAppointment.get("appointmentDate").toString()
                : appNode.path("appointmentDate").asText(root.path("documentDate").asText(null));

        if (dateStr != null && !dateStr.isBlank()) {
            try {
                appDate = java.time.LocalDate.parse(dateStr.trim().substring(0, Math.min(dateStr.trim().length(), 10)));
            } catch (Exception ignored) {}
        }

        String doctorName = (customAppointment != null && customAppointment.get("doctorName") != null)
                ? customAppointment.get("doctorName").toString()
                : appNode.path("doctorName").asText(root.path("doctorName").asText("Bác sĩ điều trị"));

        String location = (customAppointment != null && customAppointment.get("location") != null)
                ? customAppointment.get("location").toString()
                : appNode.path("location").asText(root.path("facilityName").asText("Cơ sở y tế"));

        String reason = (customAppointment != null && customAppointment.get("reason") != null)
                ? customAppointment.get("reason").toString()
                : appNode.path("reason").asText("Khám theo tài liệu: " + document.getDocumentName());

        String diagnosis = (customAppointment != null && customAppointment.get("diagnosis") != null)
                ? customAppointment.get("diagnosis").toString()
                : appNode.path("diagnosis").asText(null);

        com.healthrecord.healthrecord.dto.AppointmentDto dto = new com.healthrecord.healthrecord.dto.AppointmentDto();
        dto.setProfileId(profileId);
        dto.setAppointmentDate(appDate);
        dto.setDoctorName(doctorName);
        dto.setLocation(location);
        dto.setReason(reason);
        dto.setDiagnosis(diagnosis);

        appointmentService.saveAppointment(dto);

        // Đồng bộ vào extractedJson
        if (customAppointment != null && !customAppointment.isEmpty() && root.isObject()) {
            try {
                com.fasterxml.jackson.databind.node.ObjectNode rootObj = (com.fasterxml.jackson.databind.node.ObjectNode) root;
                com.fasterxml.jackson.databind.node.ObjectNode apNode = rootObj.has("appointment") && rootObj.get("appointment").isObject()
                        ? (com.fasterxml.jackson.databind.node.ObjectNode) rootObj.get("appointment")
                        : rootObj.putObject("appointment");
                apNode.put("appointmentDate", appDate.toString());
                apNode.put("doctorName", doctorName);
                apNode.put("location", location);
                apNode.put("reason", reason);
                if (diagnosis != null) apNode.put("diagnosis", diagnosis);
                if (customAppointment.get("doctorNote") != null) {
                    apNode.put("doctorNote", customAppointment.get("doctorNote").toString());
                }
                document.setExtractedJson(objectMapper.writeValueAsString(rootObj));
            } catch (Exception ignored) {}
        }

        // Nếu có chẩn đoán bệnh mạn tính, cập nhật vào HealthProfile nếu chưa có
        if (diagnosis != null && !diagnosis.isBlank()) {
            com.healthrecord.healthrecord.entity.HealthProfile profile = healthProfileRepository.findById(profileId).orElse(null);
            if (profile != null) {
                String currentChronic = profile.getChronicConditions();
                if (currentChronic == null || currentChronic.isBlank()) {
                    profile.setChronicConditions(diagnosis);
                    healthProfileRepository.save(profile);
                } else if (!currentChronic.toLowerCase().contains(diagnosis.toLowerCase())) {
                    profile.setChronicConditions(currentChronic + "; " + diagnosis);
                    healthProfileRepository.save(profile);
                }
            }
        }

        document.setIsApplied(true);
        documentRepository.save(document);

        return Map.of(
                "success", true,
                "message", "Đã lưu thông tin vào Lịch khám bệnh thành công!",
                "appointment", dto
        );
    }

    private Double parseDouble(Object val) {
        if (val == null) return null;
        try {
            return Double.parseDouble(val.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInteger(Object val) {
        if (val == null) return null;
        try {
            return Integer.parseInt(val.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean parseBoolean(Object val) {
        if (val == null) return null;
        try {
            return Boolean.parseBoolean(val.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }
}

