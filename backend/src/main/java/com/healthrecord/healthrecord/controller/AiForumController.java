package com.healthrecord.healthrecord.controller;

import com.healthrecord.healthrecord.dto.AiSuggestRequest;
import com.healthrecord.healthrecord.dto.AiSuggestResponse;
import com.healthrecord.healthrecord.service.AiForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiForumController {

    @Autowired
    private AiForumService aiForumService;

    @PostMapping("/suggest-post")
    public ResponseEntity<?> suggestPost(@RequestBody AiSuggestRequest request) {
        String title = request.getTitle() != null ? request.getTitle() : "";
        String content = request.getContent() != null ? request.getContent() : "";
        
        if (title.isEmpty() && content.isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập ít nhất tiêu đề hoặc nội dung nháp.");
        }
        
        String suggestion = aiForumService.suggestContent(title, content);
        
        return ResponseEntity.ok(new AiSuggestResponse(suggestion));
    }
}
