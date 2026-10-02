package com.healthrecord.healthrecord.controller.api;

import com.healthrecord.healthrecord.entity.MedicalFacility;
import com.healthrecord.healthrecord.service.MedicalFacilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facilities")
public class MedicalFacilityRestController {

    @Autowired
    private MedicalFacilityService facilityService;

    @GetMapping("/nearby")
    public ResponseEntity<List<MedicalFacility>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double radius) {
        
        List<MedicalFacility> facilities = facilityService.getNearbyFacilities(lat, lng, radius);
        return ResponseEntity.ok(facilities);
    }

    @GetMapping("/all")
    public ResponseEntity<List<MedicalFacility>> getAll() {
        return ResponseEntity.ok(facilityService.getAllFacilities());
    }

    @GetMapping("/search")
    public ResponseEntity<List<MedicalFacility>> search(@RequestParam String name) {
        return ResponseEntity.ok(facilityService.searchByName(name));
    }
}
