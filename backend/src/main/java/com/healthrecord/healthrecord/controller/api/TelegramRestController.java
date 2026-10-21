package com.healthrecord.healthrecord.controller.api;

import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.repository.UserRepository;
import com.healthrecord.healthrecord.service.TelegramNotificationService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
public class TelegramRestController {

    @Autowired
    private TelegramNotificationService telegramService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Lấy thông tin trạng thái liên kết Telegram của người dùng hiện tại
     */
    @GetMapping("/status")
    public ResponseEntity<?> getTelegramStatus() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        // Tải lại bản ghi mới nhất từ DB
        User user = userRepository.findById(currentUser.getId()).orElse(currentUser);

        boolean isConnected = user.getTelegramChatId() != null && !user.getTelegramChatId().isBlank();

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("connected", isConnected);
        res.put("chatId", user.getTelegramChatId());
        res.put("botUsername", telegramService.getBotUsername());
        res.put("botInviteLink", telegramService.getBotInviteLink(user.getId()));

        return ResponseEntity.ok(res);
    }

    /**
     * Kết nối Telegram: Cho phép nhập trực tiếp Chat ID hoặc tự động quét getUpdates
     */
    @PostMapping("/connect")
    @Transactional
    public ResponseEntity<?> connectTelegram(@RequestBody(required = false) Map<String, String> payload) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        User user = userRepository.findById(currentUser.getId()).orElseThrow();

        String targetChatId = null;
        if (payload != null && payload.containsKey("chatId") && !payload.get("chatId").isBlank()) {
            targetChatId = payload.get("chatId").trim();
        } else {
            // Tự động tìm từ các tin nhắn /start gần nhất gửi tới Bot
            targetChatId = telegramService.findChatIdFromRecentUpdates(user.getId());
        }

        if (targetChatId == null || targetChatId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Chưa phát hiện tin nhắn từ bạn trên Telegram. Vui lòng bấm vào link bot, bấm START rồi thử lại!"
            ));
        }

        user.setTelegramChatId(targetChatId);
        userRepository.save(user);

        // Bắn tin nhắn chào mừng ngay lập tức
        telegramService.sendTestNotification(targetChatId, user.getFullName());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "chatId", targetChatId,
                "message", "Liên kết Telegram thành công! Hãy kiểm tra tin nhắn xác nhận trên ứng dụng."
        ));
    }

    /**
     * Hủy liên kết Telegram
     */
    @PostMapping("/disconnect")
    @Transactional
    public ResponseEntity<?> disconnectTelegram() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        user.setTelegramChatId(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "Đã hủy liên kết Telegram thành công."));
    }

    /**
     * Gửi tin nhắn test thử nghiệm
     */
    @PostMapping("/test")
    public ResponseEntity<?> sendTestNotification() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        User user = userRepository.findById(currentUser.getId()).orElse(currentUser);
        if (user.getTelegramChatId() == null || user.getTelegramChatId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Bạn chưa liên kết tài khoản Telegram."
            ));
        }

        boolean sent = telegramService.sendTestNotification(user.getTelegramChatId(), user.getFullName());
        if (sent) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã gửi thông báo kiểm tra đến Telegram của bạn!"));
        } else {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Không thể gửi tin nhắn. Vui lòng kiểm tra lại Bot Token."));
        }
    }
}
