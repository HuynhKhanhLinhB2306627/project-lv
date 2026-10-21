package com.healthrecord.healthrecord.controller;

import com.healthrecord.healthrecord.service.AiConsultingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai-consulting")
public class AiConsultingController {

    @Autowired
    private AiConsultingService aiConsultingService;

    @PostMapping("/ask")
    public ResponseEntity<AiConsultingService.AiAdviceResponse> askAi(@RequestBody Map<String, Object> payload) {
        String question = payload.get("question") != null ? payload.get("question").toString() : null;
        Long profileId = parseProfileId(payload.get("profileId"));
        Long sessionId = parseProfileId(payload.get("sessionId")); 

        AiConsultingService.AiAdviceResponse response = aiConsultingService.getAdvice(question, profileId, sessionId);
        return ResponseEntity.ok(response);
    }

    private Long parseProfileId(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof Number) {
            return ((Number) rawValue).longValue();
        }
        try {
            String value = rawValue.toString().trim();
            if (value.isEmpty()) {
                return null;
            }
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
