package com.healthrecord.healthrecord.service;

import com.healthrecord.healthrecord.dto.MedicineDto;
import com.healthrecord.healthrecord.entity.Medicine;
import java.util.List;

public interface MedicineService {
    
    List<Medicine> findAllMedicines();

    
    List<Medicine> findPendingMedicines();

    
    void saveMedicine(MedicineDto medicineDto);

    
    void approveMedicine(Long id);

    Medicine findMedicineById(Long id);
    void deleteMedicineById(Long id);
}