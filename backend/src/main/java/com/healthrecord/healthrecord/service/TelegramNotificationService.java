package com.healthrecord.healthrecord.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramNotificationService {

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.bot.username:Suc_khoe_dien_tu_Bot}")
    private String botUsername;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TELEGRAM_API_BASE = "https://api.telegram.org/bot";

    /**
     * Gửi tin nhắn thông báo nhắc uống thuốc qua Telegram
     */
    public boolean sendMedicationReminder(String chatId, String profileName, String medicineName, String dosage, String timing, String timeStr) {
        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            return false;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🔔 <b>[NHẮC NHỞ UỐNG THUỐC]</b>\n");
        sb.append("Xin chào <b>").append(escapeHtml(profileName)).append("</b>, 30 phút nữa đến giờ uống thuốc theo phác đồ điều trị!\n\n");
        sb.append("💊 <b>Tên thuốc:</b> ").append(escapeHtml(medicineName)).append("\n");
        sb.append("📏 <b>Liều lượng:</b> ").append(escapeHtml(dosage != null ? dosage : "Theo chỉ định")).append("\n");
        sb.append("⏰ <b>Thời điểm:</b> ").append(escapeHtml(timing != null ? timing : "Đúng giờ")).append("\n");
        sb.append("⏱️ <b>Giờ uống:</b> ").append(escapeHtml(timeStr)).append("\n\n");
        sb.append("💡 <i>Lời dặn: Hãy uống cùng một cốc nước ấm đầy và nghỉ ngơi hợp lý bạn nhé!</i>\n");
        sb.append("──────────────────────\n");
        sb.append("🏥 <i>Hệ thống Quản lý Hồ sơ Sức khỏe Điện tử</i>");

        return sendMessage(chatId, sb.toString());
    }

    /**
     * Gửi tin nhắn kiểm tra kết nối Telegram
     */
    public boolean sendTestNotification(String chatId, String userName) {
        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            return false;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🎉 <b>[KẾT NỐI THÀNH CÔNG]</b>\n");
        sb.append("Xin chào <b>").append(escapeHtml(userName != null ? userName : "bạn")).append("</b>!\n\n");
        sb.append("Tài khoản của bạn đã được liên kết thành công với <b>Trợ lý Sức khỏe AI</b>.\n");
        sb.append("Từ bây giờ, hệ thống sẽ tự động nhắc nhở lịch uống thuốc và lịch khám bệnh trực tiếp qua Telegram này. 💊✨\n\n");
        sb.append("──────────────────────\n");
        sb.append("🏥 <i>Hệ thống Quản lý Hồ sơ Sức khỏe Điện tử</i>");

        return sendMessage(chatId, sb.toString());
    }

    /**
     * Gửi tin nhắn HTML tới chatId cụ thể
     */
    public boolean sendMessage(String chatId, String htmlText) {
        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            return false;
        }

        try {
            String url = TELEGRAM_API_BASE + botToken + "/sendMessage";

            Map<String, Object> body = new HashMap<>();
            body.put("chat_id", chatId.trim());
            body.put("text", htmlText);
            body.put("parse_mode", "HTML");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("[Telegram] Lỗi khi gửi tin nhắn tới chat_id " + chatId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Quét Telegram Updates để tự động tìm Chat ID khi người dùng vừa bấm /start trên bot
     */
    public String findChatIdFromRecentUpdates(Long userId) {
        if (botToken == null || botToken.isBlank()) {
            return null;
        }

        try {
            String url = TELEGRAM_API_BASE + botToken + "/getUpdates";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode result = root.path("result");

                if (result.isArray() && !result.isEmpty()) {
                    // Duyệt từ tin nhắn mới nhất ngược lại
                    for (int i = result.size() - 1; i >= 0; i--) {
                        JsonNode update = result.get(i);
                        JsonNode message = update.path("message");
                        String text = message.path("text").asText("");
                        JsonNode chat = message.path("chat");
                        String chatId = chat.path("id").asText(null);

                        // 1. Kiểm tra nếu có mã định danh userId: /start user_123 hoặc /start 123
                        if (userId != null && (text.contains("user_" + userId) || text.contains(" " + userId))) {
                            return chatId;
                        }

                        // 2. Nếu không truyền userId hoặc tìm bản ghi start gần nhất
                        if (text.startsWith("/start") && chatId != null) {
                            return chatId;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Telegram] Lỗi khi quét getUpdates: " + e.getMessage());
        }
        return null;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public String getBotInviteLink(Long userId) {
        if (userId != null) {
            return "https://t.me/" + botUsername + "?start=user_" + userId;
        }
        return "https://t.me/" + botUsername;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }
}
