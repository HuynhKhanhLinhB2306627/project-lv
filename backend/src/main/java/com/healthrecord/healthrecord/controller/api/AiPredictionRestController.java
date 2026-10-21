package com.healthrecord.healthrecord.controller.api;

import com.healthrecord.healthrecord.service.AiPredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AiPredictionRestController {

    @Autowired
    private AiPredictionService aiPredictionService;

    @PostMapping("/predict-heart-disease")
    public ResponseEntity<?> predictHeartDisease(@RequestBody Map<String, Object> payload) {
        Map<String, Object> result = aiPredictionService.predictHeartRiskFromData(payload);
        
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            
            Object riskLevel = result.get("risk_level");
            if ("CHƯA ĐỦ DỮ LIỆU".equals(riskLevel) || "LỖI DỮ LIỆU".equals(riskLevel)) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.status(500).body(result);
        }
    }
}
