package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    
    boolean existsByName(String name);

    
    List<Medicine> findByIsApprovedTrue();

    
    List<Medicine> findByIsApprovedFalse();
}