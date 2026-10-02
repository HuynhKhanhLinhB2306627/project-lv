package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.MedicalFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalFacilityRepository extends JpaRepository<MedicalFacility, Long> {

    
    @Query(value = "SELECT * FROM medical_facilities WHERE " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) * " +
            "cos(radians(longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(latitude)))) <= :radius", 
            nativeQuery = true)
    List<MedicalFacility> findNearbyFacilities(@Param("lat") double lat, 
                                               @Param("lng") double lng, 
                                               @Param("radius") double radiusInKm);

    List<MedicalFacility> findByNameContainingIgnoreCase(String name);
}
