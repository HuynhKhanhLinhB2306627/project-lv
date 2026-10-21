package com.healthrecord.healthrecord.service;

import com.healthrecord.healthrecord.entity.BodyMetric;
import com.healthrecord.healthrecord.entity.HealthProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiPredictionService {

    private final RestTemplate restTemplate;

    @Value("${ai.python.url:http://localhost:5000}")
    private String pythonAiBaseUrl;

    @Value("${ai.python.api.key}")
    private String aiServerApiKey;

    @Autowired
    private AiConsultingService aiConsultingService;

    @Autowired
    public AiPredictionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String predictHeartRisk(BodyMetric metric) {
        Map<String, Object> structured = predictHeartRiskStructured(metric);
        Object message = structured.get("message");
        Object riskPercentage = structured.get("risk_percentage");
        Object riskLevel = structured.get("risk_level");

        if (Boolean.TRUE.equals(structured.get("success"))) {
            return String.format(
                    "%s (Mức độ: %s, Tỷ lệ: %s%%)",
                    message,
                    riskLevel,
                    riskPercentage
            );
        }

        return message != null ? message.toString() : "Có lỗi xảy ra trong quá trình dự đoán.";
    }

    public Map<String, Object> predictHeartRiskStructured(BodyMetric metric) {
        if (metric == null) {
            return buildErrorResponse("Không có dữ liệu chỉ số cơ thể để dự đoán.");
        }

        HealthProfile profile = metric.getHealthProfile();
        if (profile == null) {
            return buildErrorResponse("Không tìm thấy hồ sơ sức khỏe gắn với bộ chỉ số này.");
        }

        List<String> missingFields = validateMetric(metric, profile);
        if (!missingFields.isEmpty()) {
            return buildValidationErrorResponse(missingFields);
        }

        Map<String, Object> requestData = buildPredictionRequest(metric, profile);
        return callPythonAiServer(requestData);
    }

    public Map<String, Object> predictHeartRiskFromData(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return buildErrorResponse("Dữ liệu gửi lên không hợp lệ.");
        }
        
        Map<String, Object> mappedData = new LinkedHashMap<>(data);
        if (mappedData.containsKey("systolic_bp")) {
            mappedData.put("ap_hi", mappedData.remove("systolic_bp"));
        }
        if (mappedData.containsKey("diastolic_bp")) {
            mappedData.put("ap_lo", mappedData.remove("diastolic_bp"));
        }
        if (mappedData.containsKey("glucose")) {
            mappedData.put("gluc", mappedData.remove("glucose"));
        }
        
        return callPythonAiServer(mappedData);
    }

    private Map<String, Object> callPythonAiServer(Map<String, Object> requestData) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            headers.set("X-API-Key", aiServerApiKey);

            ResponseEntity<Map> response = restTemplate.exchange(
                    pythonAiBaseUrl + "/predict",
                    HttpMethod.POST,
                    new HttpEntity<>(requestData, headers),
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return buildErrorResponse("Dịch vụ dự đoán tim mạch không phản hồi hợp lệ.");
            }

            return normalizePredictionResponse(response.getBody(), requestData);
        } catch (RestClientException ex) {
            System.err.println("Lỗi kết nối AI Prediction Service: " + ex.getMessage());
            return buildErrorResponse("Không thể kết nối đến hệ thống AI dự đoán ngay lúc này.");
        } catch (Exception ex) {
            System.err.println("Lỗi xử lý AI Prediction: " + ex.getMessage());
            return buildErrorResponse("Có lỗi xảy ra trong quá trình dự đoán tim mạch.");
        }
    }

    private Map<String, Object> buildPredictionRequest(BodyMetric metric, HealthProfile profile) {
        Map<String, Object> requestData = new LinkedHashMap<>();

        int age = Period.between(profile.getDateOfBirth(), LocalDate.now()).getYears();
        requestData.put("age", age);
        requestData.put("gender", mapGender(profile.getGender()));
        requestData.put("height", metric.getHeightInCm());
        requestData.put("weight", metric.getWeightInKg());
        requestData.put("ap_hi", metric.getSystolicBp());
        requestData.put("ap_lo", metric.getDiastolicBp());
        requestData.put("cholesterol", metric.getCholesterol());
        requestData.put("gluc", metric.getGlucose());
        requestData.put("smoke", Boolean.TRUE.equals(metric.getSmoking()) ? 1 : 0);
        requestData.put("alco", Boolean.TRUE.equals(metric.getAlcohol()) ? 1 : 0);
        requestData.put("active", Boolean.TRUE.equals(metric.getPhysicalActivity()) ? 1 : 0);
        requestData.put("profile_name", safe(profile.getProfileName()));
        requestData.put("full_name", safe(profile.getFullName()));
        return requestData;
    }

    private Map<String, Object> normalizePredictionResponse(Map responseBody, Map<String, Object> requestData) {
        Map<String, Object> result = new LinkedHashMap<>();
        boolean success = !responseBody.containsKey("error") && !responseBody.containsKey("detail");

        result.put("success", success);
        
        double prob = toRoundedDouble(responseBody.get("probability"), 0.0) * 100.0;
        result.put("risk_percentage", prob);
        
        boolean hasDisease = Boolean.TRUE.equals(responseBody.get("has_disease"));
        result.put("risk_level", hasDisease ? "CAO" : "THẤP");
        
        result.put("message", safeText(responseBody.get("warning"), success
                ? "Dự đoán tim mạch đã hoàn tất."
                : "Có lỗi xảy ra trong quá trình dự đoán."));
                
        List<String> warnings = new ArrayList<>();
        if (hasDisease) warnings.add("Mô hình AI phát hiện nguy cơ cao mắc bệnh tim mạch.");
        result.put("warning_signs", warnings);
        
        List<String> recs = new ArrayList<>();
        
        try {
            String prompt = String.format("Dựa trên chỉ số sau, đưa ra đúng 3 lời khuyên cực kỳ ngắn gọn (chỉ 1 câu đơn cho mỗi lời khuyên, không markdown, không gạch đầu dòng, không mở/kết): Huyết áp: %s/%s, Mỡ máu mức: %s, Đường mức: %s, Hút thuốc: %s, Rượu bia: %s, Hoạt động thể lực: %s. Nguy cơ tim mạch: %s%%.",
                    requestData.getOrDefault("ap_hi", "120"), requestData.getOrDefault("ap_lo", "80"), 
                    requestData.getOrDefault("cholesterol", "1"), requestData.getOrDefault("gluc", "1"), 
                    requestData.getOrDefault("smoke", "0"), requestData.getOrDefault("alco", "0"), 
                    requestData.getOrDefault("active", "1"), prob);
                    
            String aiAdvice = aiConsultingService.callGemini(prompt);
            for (String line : aiAdvice.split("\n")) {
                line = line.trim().replaceAll("^[-*•0-9.]\\s*", "");
                if (!line.isEmpty()) {
                    recs.add(line);
                }
            }
        } catch (Exception e) {
            System.err.println("Gemini error in AiPredictionService: " + e.getMessage());
        }

        // Fallback if Gemini fails or returns empty
        if (recs.isEmpty()) {
            if (prob >= 75.0) {
                recs.add("[!!!] CẦN KHÁM NGAY: Đi khám tim mạch hoặc cấp cứu nếu có triệu chứng đau ngực, khó thở.");
            } else if (prob >= 50.0) {
                recs.add("Nên đi khám tim mạch hoặc nội tổng quát sớm để được đánh giá trực tiếp.");
            } else if (prob >= 25.0) {
                recs.add("Nên theo dõi chỉ số định kỳ và trao đổi với bác sĩ nếu các chỉ số tiếp tục xấu đi.");
            } else {
                recs.add("Tiếp tục duy trì theo dõi sức khỏe định kỳ và lối sống lành mạnh.");
            }
        }
        
        result.put("recommendations", recs);
        
        result.put("explanation", new LinkedHashMap<>());
        result.put("input_summary", new LinkedHashMap<>());
        result.put("model_info", new LinkedHashMap<>());
        result.put("should_seek_medical_care", hasDisease);
        result.put("raw_input", requestData);

        if (!success) {
            result.put("error", responseBody.get("error") != null ? responseBody.get("error").toString() : "Dịch vụ dự đoán trả về lỗi không xác định.");
        }

        return result;
    }

    private List<String> validateMetric(BodyMetric metric, HealthProfile profile) {
        List<String> missing = new ArrayList<>();

        if (profile.getDateOfBirth() == null) missing.add("ngày sinh");
        if (profile.getGender() == null || profile.getGender().trim().isEmpty()) missing.add("giới tính");
        if (metric.getHeightInCm() == null || metric.getHeightInCm() <= 0) missing.add("chiều cao");
        if (metric.getWeightInKg() == null || metric.getWeightInKg() <= 0) missing.add("cân nặng");
        if (metric.getSystolicBp() == null || metric.getSystolicBp() <= 0) missing.add("huyết áp tâm thu");
        if (metric.getDiastolicBp() == null || metric.getDiastolicBp() <= 0) missing.add("huyết áp tâm trương");
        if (metric.getCholesterol() == null) missing.add("cholesterol");
        if (metric.getGlucose() == null) missing.add("glucose");

        return missing;
    }

    private Map<String, Object> buildValidationErrorResponse(List<String> missingFields) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("risk_percentage", 0.0);
        result.put("risk_level", "CHƯA ĐỦ DỮ LIỆU");
        result.put("message", "Chưa thể dự đoán tim mạch vì còn thiếu dữ liệu: " + String.join(", ", missingFields) + ".");
        result.put("warning_signs", Collections.singletonList("Cần cập nhật đầy đủ chỉ số cơ thể trước khi chạy AI dự đoán."));
        result.put("recommendations", Collections.singletonList("Bổ sung chiều cao, cân nặng, huyết áp, cholesterol và glucose mới nhất."));
        result.put("explanation", new LinkedHashMap<>());
        result.put("input_summary", new LinkedHashMap<>());
        result.put("model_info", new LinkedHashMap<>());
        result.put("should_seek_medical_care", false);
        return result;
    }

    private Map<String, Object> buildErrorResponse(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("risk_percentage", 0.0);
        result.put("risk_level", "LỖI HỆ THỐNG");
        result.put("message", message);
        result.put("warning_signs", new ArrayList<>());
        result.put("recommendations", Collections.singletonList("Thử lại sau hoặc kiểm tra kết nối tới AI Prediction Service."));
        result.put("explanation", new LinkedHashMap<>());
        result.put("input_summary", new LinkedHashMap<>());
        result.put("model_info", new LinkedHashMap<>());
        result.put("should_seek_medical_care", false);
        return result;
    }

    private int mapGender(String gender) {
        if (gender == null) {
            return 0;
        }

        String normalized = gender.trim().toLowerCase();
        if (normalized.equals("nam") || normalized.equals("male") || normalized.equals("m")) {
            return 1;
        }
        return 0;
    }

    private double toRoundedDouble(Object value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Math.round(Double.parseDouble(value.toString()) * 10.0) / 10.0;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private String safeText(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? defaultValue : text;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
