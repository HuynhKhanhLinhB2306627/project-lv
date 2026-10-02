package com.healthrecord.healthrecord.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

    public void sendMedicationReminder(String to, String profileName, String medicineName, String dosage, String timing, String time) {
        String subject = "Lịch nhắc uống thuốc: " + medicineName + " - " + profileName;
        String body = "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 10px; overflow: hidden;'>" +
                "<div style='background-color: #007bff; color: white; padding: 20px; text-align: center;'>" +
                "<h2>Nhắc nhở uống thuốc</h2>" +
                "</div>" +
                "<div style='padding: 20px; color: #333;'>" +
                "<p>Xin chào,</p>" +
                "<p>Đây là thông báo nhắc bạn uống thuốc cho hồ sơ: <strong>" + profileName + "</strong></p>" +
                "<div style='background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                "<p><strong>Thuốc:</strong> " + medicineName + "</p>" +
                "<p><strong>Liều lượng:</strong> " + dosage + "</p>" +
                "<p><strong>Thời điểm:</strong> " + timing + "</p>" +
                "<p><strong>Giờ uống:</strong> <span style='color: #d9534f; font-weight: bold;'>" + time + "</span></p>" +
                "</div>" +
                "<p>Vui lòng uống thuốc đúng giờ để đảm bảo hiệu quả điều trị.</p>" +
                "<p>Trân trọng,<br>Gemini Health Record</p>" +
                "</div>" +
                "<div style='background-color: #f1f1f1; padding: 10px; text-align: center; font-size: 12px; color: #777;'>" +
                "Đây là email tự động, vui lòng không trả lời email này." +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
        
        sendEmail(to, subject, body);
    }
}
