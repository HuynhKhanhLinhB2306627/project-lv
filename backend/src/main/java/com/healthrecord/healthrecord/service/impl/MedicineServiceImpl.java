package com.healthrecord.healthrecord.service.impl;

import com.healthrecord.healthrecord.dto.MedicineDto;
import com.healthrecord.healthrecord.entity.Medicine;
import com.healthrecord.healthrecord.repository.MedicineRepository;
import com.healthrecord.healthrecord.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MedicineServiceImpl implements MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    @Override
    public List<Medicine> findAllMedicines() {
        
        return medicineRepository.findByIsApprovedTrue();
    }

    @Override
    public List<Medicine> findPendingMedicines() {
        
        return medicineRepository.findByIsApprovedFalse();
    }

    @Override
    public void saveMedicine(MedicineDto dto) {
        Medicine medicine;
        if (dto.getId() != null) {
            
            medicine = medicineRepository.findById(dto.getId()).orElse(new Medicine());
        } else {
            
            if (medicineRepository.existsByName(dto.getName())) {
                throw new IllegalArgumentException("Tên thuốc đã tồn tại trong hệ thống.");
            }
            medicine = new Medicine();
            
            medicine.setApproved(false);
        }

        medicine.setName(dto.getName());
        medicine.setUnit(dto.getUnit());
        medicine.setDescription(dto.getDescription());

        medicineRepository.save(medicine);
    }

    @Override
    public void approveMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id).orElse(null);
        if (medicine != null) {
            medicine.setApproved(true);
            medicineRepository.save(medicine);
        }
    }

    @Override
    public Medicine findMedicineById(Long id) {
        return medicineRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteMedicineById(Long id) {
        medicineRepository.deleteById(id);
    }
}