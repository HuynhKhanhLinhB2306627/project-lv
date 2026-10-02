package com.healthrecord.healthrecord.service;

import com.healthrecord.healthrecord.entity.MedicalFacility;
import com.healthrecord.healthrecord.repository.MedicalFacilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class MedicalFacilityService {

    @Autowired
    private MedicalFacilityRepository repository;

    public List<MedicalFacility> getNearbyFacilities(double lat, double lng, double radius) {
        List<MedicalFacility> facilities = repository.findNearbyFacilities(lat, lng, radius);
        
        for (MedicalFacility f : facilities) {
            f.setDistance(calculateHaversine(lat, lng, f.getLatitude(), f.getLongitude()));
        }
        
        facilities.sort(Comparator.comparingDouble(MedicalFacility::getDistance));
        return facilities;
    }

    public List<MedicalFacility> getAllFacilities() {
        return repository.findAll();
    }

    public List<MedicalFacility> searchByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }

    private double calculateHaversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; 
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
