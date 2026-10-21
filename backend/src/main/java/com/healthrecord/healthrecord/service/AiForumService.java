package com.healthrecord.healthrecord.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiForumService {

    @Autowired
    private AiConsultingService aiConsultingService;

    public String suggestContent(String title, String currentContent) {

        String prompt = String.format(
            "Bạn là một chuyên gia hỗ trợ viết bài trên diễn đàn sức khỏe chuyên nghiệp. " +
            "Người dùng đã nhập tiêu đề: '%s' và nội dung nháp: '%s'. " +
            "Hãy giúp họ mở rộng và viết lại nội dung này sao cho mạch lạc, đầy đủ thông tin y khoa cơ bản (nhưng không chẩn đoán bệnh), " +
            "giọng văn lịch sự, tích cực và dễ hiểu. " +
            "Yêu cầu: " +
            "1. Giữ nguyên ý định của người dùng. " +
            "2. Cấu trúc bài viết gồm: Giới thiệu tình trạng, chi tiết triệu chứng/thắc mắc, và lời kết mong muốn được tư vấn. " +
            "3. Không sử dụng các từ ngữ quá chuyên môn gây khó hiểu. " +
            "4. Đảm bảo độ dài khoảng 150-300 từ. " +
            "5. CHỈ TRẢ VỀ NỘI DUNG BÀI VIẾT, không thêm lời chào của AI.",
            title, currentContent
        );

        try {
            return aiConsultingService.callGemini(prompt);
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi kết nối AI: " + e.getMessage();
        }
    }
}
