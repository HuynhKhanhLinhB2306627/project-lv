package com.healthrecord.healthrecord.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthrecord.healthrecord.entity.AiChatMessage;
import com.healthrecord.healthrecord.entity.AiChatSession;
import com.healthrecord.healthrecord.entity.Appointment;
import com.healthrecord.healthrecord.entity.BodyMetric;
import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.MedicationCourse;
import com.healthrecord.healthrecord.entity.Medicine;
import com.healthrecord.healthrecord.entity.ProfileAccess;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.entity.Vaccination;
import com.healthrecord.healthrecord.repository.AiChatSessionRepository;
import com.healthrecord.healthrecord.repository.AppointmentRepository;
import com.healthrecord.healthrecord.repository.VaccinationRepository;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AiConsultingService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HealthProfileService healthProfileService;

    @Autowired
    private BodyMetricService bodyMetricService;

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private ProfileAccessService profileAccessService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private VaccinationRepository vaccinationRepository;

    @Autowired
    private com.healthrecord.healthrecord.repository.DocumentRepository documentRepository;

    @Autowired
    private com.healthrecord.healthrecord.repository.AiChatSessionRepository aiChatSessionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getAdvice(String userQuestion) {
        return getAdvice(userQuestion, null, null).getAnswer();
    }

    public AiAdviceResponse getAdvice(String userQuestion, Long profileId, Long sessionId) {
        String normalizedQuestion = userQuestion != null ? userQuestion.trim() : "";
        if (normalizedQuestion.isEmpty()) {
            return AiAdviceResponse.validation("Vui lòng nhập câu hỏi trước khi gửi.");
        }

        HealthProfile profile = resolveProfile(profileId);
        if (profile == null) {
            return AiAdviceResponse.validation("Bạn chưa có hồ sơ phù hợp hoặc không có quyền truy cập hồ sơ được chọn.");
        }

        ProfileAccess.AccessLevel accessLevel = resolveAccessLevel(profile);
        
        
        User currentUser = SecurityUtils.getCurrentUser();
        AiChatSession session = null;
        if (currentUser != null) {
            if (sessionId != null) {
                session = aiChatSessionRepository.findById(sessionId).orElse(null);
            }
            if (session == null) {
                session = new AiChatSession();
                session.setUser(currentUser);
                session.setTitle(normalizedQuestion.length() > 30 ? normalizedQuestion.substring(0, 27) + "..." : normalizedQuestion);
                session = aiChatSessionRepository.save(session);
            }
            AiChatMessage userMsg = new AiChatMessage();
            userMsg.setSession(session);
            userMsg.setRole(AiChatMessage.Role.USER);
            userMsg.setContent(normalizedQuestion);
            session.getMessages().add(userMsg);
        }

        EmergencyCheck emergencyCheck = evaluateEmergency(normalizedQuestion);
        if (emergencyCheck.isEmergency()) {
            AiAdviceResponse emergencyRes = AiAdviceResponse.emergency(emergencyCheck.getMessage(), profile.getId(), accessLevel.name());
            emergencyRes.setSessionId(session != null ? session.getId() : null);
            saveAiResponseToHistory(session, emergencyRes);
            return emergencyRes;
        }

        String healthContext = buildHealthContext(profile, accessLevel);
        String prompt = buildPrompt(normalizedQuestion, healthContext, accessLevel, session);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            AiAdviceResponse errRes = AiAdviceResponse.systemError(
                    "Chưa cấu hình Gemini API key. Hãy đặt biến môi trường GEMINI_API_KEY trước khi dùng AI.",
                    profile.getId(),
                    accessLevel.name()
            );
            errRes.setSessionId(session != null ? session.getId() : null);
            return errRes;
        }

        try {
            String rawText = callGemini(prompt);
            AiAdviceResponse response = parseAiResponse(rawText);
            response.setProfileId(profile.getId());
            response.setAccessLevel(accessLevel.name());
            response.setSuccess(true);
            response.setSessionId(session != null ? session.getId() : null);
            
            saveAiResponseToHistory(session, response);
            
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = "Lỗi kết nối AI: " + e.getMessage();
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                errorMsg = "Không tìm thấy mô hình AI (404). Vui lòng kiểm tra lại phiên bản model trong cấu hình.";
            } else if (e.getMessage() != null && e.getMessage().contains("403")) {
                errorMsg = "Lỗi xác thực (403). Vui lòng kiểm tra lại API Key.";
            } else if (e.getMessage() != null && (e.getMessage().contains("503") || e.getMessage().contains("UNAVAILABLE") || e.getMessage().contains("high demand"))) {
                errorMsg = "Hệ thống AI đang quá tải. Vui lòng thử lại sau ít phút. 🙏";
            }
            AiAdviceResponse errRes = AiAdviceResponse.systemError(
                    errorMsg,
                    profile.getId(),
                    accessLevel.name()
            );
            errRes.setSessionId(session != null ? session.getId() : null);
            return errRes;
        }
    }

    private void saveAiResponseToHistory(AiChatSession session, AiAdviceResponse response) {
        if (session != null) {
            AiChatMessage botMsg = new AiChatMessage();
            botMsg.setSession(session);
            botMsg.setRole(AiChatMessage.Role.BOT);
            
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("content", response.getAnswer());
                data.put("warningSigns", response.getWarningSigns());
                data.put("recommendations", response.getRecommendations());
                String json = objectMapper.writeValueAsString(data);
                botMsg.setContent(json);
            } catch (Exception e) {
                botMsg.setContent(response.getAnswer());
            }

            session.getMessages().add(botMsg);
            aiChatSessionRepository.save(session);
        }
    }

    public List<AiChatSession> getUserChatSessions() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) return java.util.Collections.emptyList();
        return aiChatSessionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public AiChatSession getSessionById(Long sessionId) {
        User user = SecurityUtils.getCurrentUser();
        return aiChatSessionRepository.findById(sessionId)
                .filter(s -> s.getUser().getId().equals(user.getId()))
                .orElse(null);
    }

    @Transactional
    public void deleteSession(Long sessionId) {
        User user = SecurityUtils.getCurrentUser();
        aiChatSessionRepository.findById(sessionId)
                .filter(s -> s.getUser().getId().equals(user.getId()))
                .ifPresent(aiChatSessionRepository::delete);
    }

    @Transactional
    public void deleteAllUserSessions() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) return;
        List<AiChatSession> sessions = aiChatSessionRepository.findByUserOrderByCreatedAtDesc(user);
        aiChatSessionRepository.deleteAll(sessions);
    }

    public String callGemini(String prompt) throws Exception {
        int maxRetries = 2; 
        int retryDelayMs = 2000; 
        
        Exception lastException = null;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                Map<String, Object> part = new HashMap<>();
                part.put("text", prompt);

                Map<String, Object> content = new HashMap<>();
                content.put("parts", Collections.singletonList(part));

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("contents", Collections.singletonList(content));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
                String fullUrl = apiUrl + apiKey;
                ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, requestEntity, String.class);

                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (!candidates.isArray() || candidates.isEmpty()) {
                    throw new IllegalStateException("Gemini không trả về candidate hợp lệ.");
                }

                JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
                if (textNode.isMissingNode() || textNode.asText().trim().isEmpty()) {
                    throw new IllegalStateException("Gemini không trả về nội dung văn bản.");
                }

                return textNode.asText();
                
            } catch (Exception e) {
                lastException = e;
                String errMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                
                
                if ((errMsg.contains("503") || errMsg.contains("unavailable") || errMsg.contains("high demand")) 
                    && attempt < maxRetries) {
                    System.out.println("Gemini API bị quá tải (503), thử lại lần " + (attempt + 2) + "...");
                    try {
                        Thread.sleep(retryDelayMs * (attempt + 1)); 
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue; 
                }
                
                
                throw e;
            }
        }
        
        
        throw lastException != null ? lastException : new Exception("Không thể kết nối tới Gemini API");
    }

    private HealthProfile resolveProfile(Long requestedProfileId) {
        if (requestedProfileId != null) {
            HealthProfile selectedProfile = healthProfileService.findProfileById(requestedProfileId);
            if (selectedProfile != null) {
                return selectedProfile;
            }
        }

        List<HealthProfile> profiles = healthProfileService.getAllAccessibleProfiles();
        if (profiles.isEmpty()) {
            return null;
        }
        return healthProfileService.findProfileById(profiles.get(0).getId());
    }

    private ProfileAccess.AccessLevel resolveAccessLevel(HealthProfile profile) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || profile == null) {
            return ProfileAccess.AccessLevel.VIEWER;
        }

        if (profile.getUser() != null && Objects.equals(profile.getUser().getId(), currentUser.getId())) {
            return ProfileAccess.AccessLevel.OWNER;
        }

        if (profileAccessService.hasEditorAccess(currentUser.getId(), profile.getId())) {
            return ProfileAccess.AccessLevel.EDITOR;
        }

        boolean hasSharedAccess = profileAccessService.getAccessListByProfileId(profile.getId()).stream()
                .anyMatch(access -> access.getUser() != null && Objects.equals(access.getUser().getId(), currentUser.getId()));

        return hasSharedAccess ? ProfileAccess.AccessLevel.VIEWER : ProfileAccess.AccessLevel.VIEWER;
    }

    private String buildHealthContext(HealthProfile profile, ProfileAccess.AccessLevel accessLevel) {
        StringBuilder sb = new StringBuilder();
        sb.append("[Thông tin hồ sơ]\n");
        sb.append("- Hồ sơ đang được tư vấn cho profileId: ").append(profile.getId()).append("\n");
        sb.append("- Mức quyền hiện tại: ").append(accessLevel.name()).append("\n");
        sb.append("- Tên hồ sơ: ").append(safe(profile.getProfileName())).append("\n");
        sb.append("- Họ tên: ").append(safe(profile.getFullName())).append("\n");
        sb.append("- Tuổi: ").append(formatAge(profile.getDateOfBirth())).append("\n");
        sb.append("- Giới tính: ").append(safe(profile.getGender())).append("\n");
        sb.append("- Nhóm máu: ").append(safe(profile.getBloodType())).append("\n");
        sb.append("- Bệnh lý nền: ").append(safe(profile.getChronicConditions())).append("\n");

        appendBodyMetrics(sb, profile.getId());
        appendMedicationContext(sb, profile.getId());
        appendAppointmentContext(sb, profile);
        appendVaccinationContext(sb, profile);
        appendDocumentContext(sb, profile);

        sb.append("\n[Nguyên tắc trả lời]\n");
        sb.append("- Chỉ tham khảo, không thay thế bác sĩ.\n");
        sb.append("- Không chẩn đoán xác định bệnh.\n");
        sb.append("- Không kê đơn hoặc đưa liều thuốc mới ngoài dữ liệu hiện có.\n");
        sb.append("- Nếu dữ liệu hồ sơ chưa đủ, phải nói rõ là chưa đủ dữ liệu.\n");

        return sb.toString();
    }

    private void appendBodyMetrics(StringBuilder sb, Long profileId) {
        sb.append("\n[Chỉ số cơ thể]\n");
        Optional<BodyMetric> latestOpt = bodyMetricService.findLatestBodyMetricByProfileId(profileId);
        if (latestOpt.isEmpty()) {
            sb.append("- Chưa có dữ liệu body metric gần đây.\n");
            return;
        }

        BodyMetric latest = latestOpt.get();
        sb.append("- Gần nhất: chiều cao ").append(formatNumber(latest.getHeightInCm(), " cm"))
                .append(", cân nặng ").append(formatNumber(latest.getWeightInKg(), " kg"))
                .append(", BMI ").append(formatDecimal(latest.getBmi()))
                .append(", huyết áp ").append(formatBloodPressure(latest.getSystolicBp(), latest.getDiastolicBp()))
                .append(", cholesterol ").append(formatInt(latest.getCholesterol()))
                .append(", glucose ").append(formatInt(latest.getGlucose()))
                .append(", hút thuốc ").append(formatBoolean(latest.getSmoking()))
                .append(", rượu bia ").append(formatBoolean(latest.getAlcohol()))
                .append(", vận động ").append(formatBoolean(latest.getPhysicalActivity()))
                .append(".\n");

        List<BodyMetric> history = bodyMetricService.findAllBodyMetricsByProfileId(profileId);
        if (!history.isEmpty()) {
            List<BodyMetric> recent = history.stream()
                    .filter(Objects::nonNull)
                    .sorted((a, b) -> {
                        LocalDateTime aDate = a.getLogDate();
                        LocalDateTime bDate = b.getLogDate();
                        if (aDate == null && bDate == null) return 0;
                        if (aDate == null) return 1;
                        if (bDate == null) return -1;
                        return bDate.compareTo(aDate);
                    })
                    .limit(5)
                    .collect(Collectors.toList());

            sb.append("- Lịch sử gần đây: ");
            if (recent.isEmpty()) {
                sb.append("Không có.\n");
            } else {
                List<String> items = new ArrayList<>();
                for (BodyMetric metric : recent) {
                    items.add(String.format(Locale.US,
                            "%s | cân nặng: %s kg | BMI: %s | HA: %s",
                            metric.getLogDate() != null ? metric.getLogDate() : "Không rõ ngày",
                            metric.getWeightInKg() != null ? metric.getWeightInKg() : "?",
                            formatDecimal(metric.getBmi()),
                            formatBloodPressure(metric.getSystolicBp(), metric.getDiastolicBp())
                    ));
                }
                sb.append(String.join(" ; ", items)).append(".\n");
            }
        }
    }

    private void appendMedicationContext(StringBuilder sb, Long profileId) {
        sb.append("\n[Thuốc đang dùng]\n");
        List<MedicationCourse> courses = medicationService.findAllCoursesByProfileId(profileId);
        if (courses == null || courses.isEmpty()) {
            sb.append("- Chưa có liệu trình thuốc.\n");
            return;
        }

        List<MedicationCourse> activeCourses = courses.stream()
                .filter(Objects::nonNull)
                .filter(course -> course.getStatus() == MedicationCourse.CourseStatus.ACTIVE)
                .limit(5)
                .collect(Collectors.toList());

        if (activeCourses.isEmpty()) {
            sb.append("- Không có liệu trình ACTIVE, nhưng hồ sơ có lịch sử dùng thuốc.\n");
            return;
        }

        for (MedicationCourse course : activeCourses) {
            Medicine medicine = course.getMedicine();
            String medicineName = medicine != null && hasText(medicine.getName())
                    ? medicine.getName()
                    : "Thuốc chưa rõ tên";
            String unit = medicine != null && hasText(medicine.getUnit()) ? medicine.getUnit() : null;
            String description = medicine != null && hasText(medicine.getDescription()) ? medicine.getDescription() : null;

            sb.append("- ").append(medicineName);
            if (unit != null) {
                sb.append(" (đơn vị: ").append(unit).append(")");
            }
            sb.append(", liều: ").append(safe(course.getDosage()));
            sb.append(", tần suất: ").append(safe(course.getFrequency()));
            sb.append(", thời điểm: ").append(safe(course.getTiming()));
            sb.append(", từ ").append(course.getStartDate() != null ? course.getStartDate() : "không rõ");
            sb.append(" đến ").append(course.getEndDate() != null ? course.getEndDate() : "không rõ");
            if (description != null) {
                sb.append(", công dụng: ").append(description);
            }
            sb.append(".\n");
        }
    }

    private void appendAppointmentContext(StringBuilder sb, HealthProfile profile) {
        sb.append("\n[Lịch khám]\n");
        List<Appointment> appointments = appointmentRepository.findByHealthProfileOrderByAppointmentDateDesc(profile);
        if (appointments == null || appointments.isEmpty()) {
            sb.append("- Chưa có lịch sử khám.\n");
            return;
        }

        Appointment latest = appointments.get(0);
        sb.append("- Lần khám gần nhất: ngày ")
                .append(latest.getAppointmentDate() != null ? latest.getAppointmentDate() : "không rõ")
                .append(", bác sĩ: ").append(safe(latest.getDoctorName()))
                .append(", nơi khám: ").append(safe(latest.getLocation()))
                .append(", lý do: ").append(safe(latest.getReason()))
                .append(", chẩn đoán: ").append(safe(latest.getDiagnosis()))
                .append(".\n");
    }

    private void appendVaccinationContext(StringBuilder sb, HealthProfile profile) {
        sb.append("\n[Tiêm chủng]\n");
        List<Vaccination> vaccinations = vaccinationRepository.findByHealthProfileOrderByVaccinationDateDesc(profile);
        if (vaccinations == null || vaccinations.isEmpty()) {
            sb.append("- Chưa có dữ liệu tiêm chủng.\n");
            return;
        }

        Vaccination latest = vaccinations.get(0);
        sb.append("- Gần nhất: ")
                .append(safe(latest.getVaccineName()))
                .append(", mũi ").append(latest.getDoseNumber() != null ? latest.getDoseNumber() : "không rõ")
                .append(", ngày ").append(latest.getVaccinationDate() != null ? latest.getVaccinationDate() : "không rõ")
                .append(", nơi tiêm: ").append(safe(latest.getLocation()))
                .append(".\n");
    }

    private void appendDocumentContext(StringBuilder sb, HealthProfile profile) {
        sb.append("\n[Tài liệu & Kết quả xét nghiệm y tế gần nhất]\n");
        List<com.healthrecord.healthrecord.entity.Document> docs = documentRepository.findByHealthProfileOrderByUploadDateDesc(profile);
        if (docs == null || docs.isEmpty()) {
            sb.append("- Chưa có tài liệu y tế lưu trữ.\n");
            return;
        }

        int count = 0;
        for (com.healthrecord.healthrecord.entity.Document doc : docs) {
            if (doc.getExtractionStatus() == com.healthrecord.healthrecord.entity.Document.ExtractionStatus.COMPLETED
                    && hasText(doc.getSummary())) {
                sb.append("- ").append(doc.getDocumentName())
                        .append(" [").append(doc.getDocumentType() != null ? doc.getDocumentType() : "Khác").append("]: ")
                        .append(doc.getSummary().trim()).append("\n");
                count++;
                if (count >= 3) break;
            }
        }

        if (count == 0) {
            sb.append("- Có ").append(docs.size()).append(" tài liệu y tế lưu trữ nhưng chưa trích xuất tóm tắt.\n");
        }
    }

    private String buildPrompt(String userQuestion, String healthContext, ProfileAccess.AccessLevel accessLevel, AiChatSession session) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là trợ lý tư vấn sức khỏe AI cho ứng dụng quản lý hồ sơ y tế.\n")
          .append("Trả lời bằng tiếng Việt, ngắn gọn, rõ ràng, thực tế.\n")
          .append("Không được trả HTML, không markdown, không code block.\n")
          .append("Không chẩn đoán xác định. Không kê đơn mới. Không tự suy diễn dữ liệu thiếu.\n")
          .append("Nếu có dấu hiệu cần khám, phải nói rõ khi nào nên đi khám.\n")
          .append("Mức quyền của người hỏi với hồ sơ này là: ").append(accessLevel.name()).append("\n\n")
          .append("THÔNG TIN HỒ SƠ:\n")
          .append(healthContext).append("\n");

        if (session != null && session.getMessages() != null && session.getMessages().size() > 1) {
            sb.append("\n[LỊCH SỬ HỘI THOẠI TRONG PHIÊN NÀY]:\n");
            List<AiChatMessage> history = session.getMessages();
            int end = history.size() - 1; // Tin nhắn hiện tại ở cuối danh sách
            int start = Math.max(0, end - 6);
            for (int i = start; i < end; i++) {
                AiChatMessage m = history.get(i);
                if (m.getRole() == AiChatMessage.Role.USER) {
                    sb.append("- Người dùng: ").append(m.getContent()).append("\n");
                } else if (m.getRole() == AiChatMessage.Role.BOT) {
                    String botContent = m.getContent();
                    if (botContent != null && botContent.trim().startsWith("{")) {
                        try {
                            JsonNode node = objectMapper.readTree(botContent);
                            botContent = node.path("content").asText(botContent);
                        } catch (Exception ignored) {}
                    }
                    sb.append("- AI: ").append(botContent).append("\n");
                }
            }
        }

        sb.append("\nCÂU HỎI HIỆN TẠI CỦA NGƯỜI DÙNG:\n")
          .append(userQuestion).append("\n\n")
          .append("Hãy trả kết quả DUY NHẤT dưới dạng JSON hợp lệ với schema sau:\n")
          .append("{\n")
          .append("  \"answer\": \"...\",\n")
          .append("  \"recommendations\": [\"...\", \"...\"],\n")
          .append("  \"warningSigns\": [\"...\"],\n")
          .append("  \"seekMedicalHelp\": true,\n")
          .append("  \"disclaimer\": \"...\"\n")
          .append("}\n")
          .append("Nếu không đủ dữ liệu, vẫn phải trả JSON đúng schema và nói rõ dữ liệu nào còn thiếu.");

        return sb.toString();
    }

    private EmergencyCheck evaluateEmergency(String question) {
        String normalized = question == null ? "" : question.toLowerCase(Locale.ROOT);
        List<String> keywords = List.of(
                "đau ngực", "khó thở", "ngất", "bất tỉnh", "co giật", "liệt", "méo miệng",
                "nói khó", "xuất huyết", "chảy máu nhiều", "tự sát", "muốn chết", "ngộ độc",
                "không thở", "đau đầu dữ dội"
        );

        for (String keyword : keywords) {
            if (normalized.contains(keyword)) {
                return new EmergencyCheck(true,
                        "Triệu chứng bạn mô tả có thể thuộc tình huống khẩn cấp. Hãy đi cấp cứu hoặc liên hệ cơ sở y tế gần nhất ngay lập tức. "
                                + "Nếu có đau ngực dữ dội, khó thở, ngất, co giật, chảy máu nhiều hoặc ý nghĩ tự làm hại bản thân, không nên chờ AI tư vấn thêm.");
            }
        }
        return new EmergencyCheck(false, null);
    }

    private AiAdviceResponse parseAiResponse(String rawText) {
        String cleaned = cleanupModelText(rawText);
        try {
            Map<String, Object> data = objectMapper.readValue(cleaned, new TypeReference<Map<String, Object>>() {});
            AiAdviceResponse response = new AiAdviceResponse();
            response.setAnswer(sanitizeText(asString(data.get("answer"))));
            response.setRecommendations(sanitizeList(asStringList(data.get("recommendations"))));
            response.setWarningSigns(sanitizeList(asStringList(data.get("warningSigns"))));
            response.setSeekMedicalHelp(asBoolean(data.get("seekMedicalHelp")));
            response.setDisclaimer(sanitizeText(asString(data.get("disclaimer"))));
            response.setEmergency(false);
            response.setSuccess(true);

            if (!hasText(response.getAnswer())) {
                response.setAnswer("AI chưa trả về nội dung rõ ràng. Vui lòng thử diễn đạt câu hỏi chi tiết hơn.");
            }
            if (!hasText(response.getDisclaimer())) {
                response.setDisclaimer("Thông tin chỉ mang tính tham khảo, không thay thế bác sĩ.");
            }
            return response;
        } catch (Exception ex) {
            AiAdviceResponse fallback = new AiAdviceResponse();
            fallback.setSuccess(true);
            fallback.setEmergency(false);
            fallback.setAnswer(sanitizeText(cleaned));
            fallback.setRecommendations(Collections.emptyList());
            fallback.setWarningSigns(Collections.emptyList());
            fallback.setSeekMedicalHelp(false);
            fallback.setDisclaimer("Thông tin chỉ mang tính tham khảo, không thay thế bác sĩ.");
            return fallback;
        }
    }

    private String cleanupModelText(String text) {
        return MedicalDocumentExtractionService.cleanupModelText(text);
    }

    private String safe(String value) {
        return hasText(value) ? value.trim() : "Chưa có";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String formatAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return "Chưa có";
        }
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        return age + " tuổi";
    }

    private String formatNumber(Number value, String suffix) {
        return value == null ? "Chưa có" : value + suffix;
    }

    private String formatInt(Integer value) {
        return value == null ? "Chưa có" : String.valueOf(value);
    }

    private String formatDecimal(Double value) {
        return value == null ? "Chưa có" : String.format(Locale.US, "%.1f", value);
    }

    private String formatBloodPressure(Integer systolic, Integer diastolic) {
        if (systolic == null || diastolic == null) {
            return "Chưa có";
        }
        return systolic + "/" + diastolic + " mmHg";
    }

    private String formatBoolean(Boolean value) {
        if (value == null) {
            return "Chưa có";
        }
        return value ? "Có" : "Không";
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private List<String> asStringList(Object value) {
        if (value instanceof List<?>) {
            List<?> rawList = (List<?>) value;
            List<String> result = new ArrayList<>();
            for (Object item : rawList) {
                if (item != null) {
                    result.add(item.toString());
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    private boolean asBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value != null) {
            return Boolean.parseBoolean(value.toString());
        }
        return false;
    }

    private String sanitizeText(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replaceAll("[\u0000-\u001F]", " ")
                .replaceAll("\s+", " ")
                .trim();
    }

    private List<String> sanitizeList(List<String> input) {
        if (input == null || input.isEmpty()) {
            return Collections.emptyList();
        }
        return input.stream()
                .filter(Objects::nonNull)
                .map(this::sanitizeText)
                .filter(this::hasText)
                .limit(6)
                .collect(Collectors.toList());
    }

    private static class EmergencyCheck {
        private final boolean emergency;
        private final String message;

        private EmergencyCheck(boolean emergency, String message) {
            this.emergency = emergency;
            this.message = message;
        }

        public boolean isEmergency() {
            return emergency;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class AiAdviceResponse {
        private boolean success;
        private boolean emergency;
        private boolean seekMedicalHelp;
        private Long profileId;
        private String accessLevel;
        private String answer;
        private String disclaimer;
        private List<String> recommendations = new ArrayList<>();
        private List<String> warningSigns = new ArrayList<>();

        @com.fasterxml.jackson.annotation.JsonProperty("sessionId")
        private Long sessionId;

        @com.fasterxml.jackson.annotation.JsonProperty("sessionId")
        public Long getSessionId() { return sessionId; }

        @com.fasterxml.jackson.annotation.JsonProperty("sessionId")
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

        public static AiAdviceResponse validation(String message) {
            AiAdviceResponse response = new AiAdviceResponse();
            response.setSuccess(false);
            response.setEmergency(false);
            response.setSeekMedicalHelp(false);
            response.setAnswer(message);
            response.setDisclaimer("Thông tin chỉ mang tính tham khảo, không thay thế bác sĩ.");
            return response;
        }

        public static AiAdviceResponse systemError(String message, Long profileId, String accessLevel) {
            AiAdviceResponse response = validation(message);
            response.setProfileId(profileId);
            response.setAccessLevel(accessLevel);
            return response;
        }

        public static AiAdviceResponse emergency(String message, Long profileId, String accessLevel) {
            AiAdviceResponse response = new AiAdviceResponse();
            response.setSuccess(true);
            response.setEmergency(true);
            response.setSeekMedicalHelp(true);
            response.setProfileId(profileId);
            response.setAccessLevel(accessLevel);
            response.setAnswer(message);
            response.setDisclaimer("Đây là cảnh báo an toàn khẩn cấp. Hãy ưu tiên liên hệ bác sĩ hoặc cơ sở y tế gần nhất.");
            response.setRecommendations(List.of(
                    "Nếu triệu chứng nặng lên, gọi cấp cứu hoặc đến cơ sở y tế gần nhất ngay.",
                    "Không tự lái xe nếu đang chóng mặt, khó thở hoặc đau ngực dữ dội.",
                    "Mang theo thông tin thuốc đang dùng và hồ sơ bệnh lý nếu có."
            ));
            response.setWarningSigns(List.of(
                    "Đau ngực dữ dội hoặc kéo dài",
                    "Khó thở, tím tái, ngất hoặc lơ mơ",
                    "Chảy máu nhiều, co giật hoặc yếu liệt đột ngột"
            ));
            return response;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public boolean isEmergency() {
            return emergency;
        }

        public void setEmergency(boolean emergency) {
            this.emergency = emergency;
        }

        public boolean isSeekMedicalHelp() {
            return seekMedicalHelp;
        }

        public void setSeekMedicalHelp(boolean seekMedicalHelp) {
            this.seekMedicalHelp = seekMedicalHelp;
        }

        public Long getProfileId() {
            return profileId;
        }

        public void setProfileId(Long profileId) {
            this.profileId = profileId;
        }

        public String getAccessLevel() {
            return accessLevel;
        }

        public void setAccessLevel(String accessLevel) {
            this.accessLevel = accessLevel;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public String getDisclaimer() {
            return disclaimer;
        }

        public void setDisclaimer(String disclaimer) {
            this.disclaimer = disclaimer;
        }

        public List<String> getRecommendations() {
            return recommendations;
        }

        public void setRecommendations(List<String> recommendations) {
            this.recommendations = recommendations != null ? recommendations : new ArrayList<>();
        }

        public List<String> getWarningSigns() {
            return warningSigns;
        }

        public void setWarningSigns(List<String> warningSigns) {
            this.warningSigns = warningSigns != null ? warningSigns : new ArrayList<>();
        }

        public Map<String, Object> toLegacyMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("success", success);
            map.put("emergency", emergency);
            map.put("seekMedicalHelp", seekMedicalHelp);
            map.put("profileId", profileId);
            map.put("accessLevel", accessLevel);
            map.put("answer", answer);
            map.put("disclaimer", disclaimer);
            map.put("recommendations", recommendations);
            map.put("warningSigns", warningSigns);
            return map;
        }
    }
}
