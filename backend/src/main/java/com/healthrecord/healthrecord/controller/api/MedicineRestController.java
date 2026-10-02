package com.healthrecord.healthrecord.controller.api;

import com.healthrecord.healthrecord.entity.Medicine;
import com.healthrecord.healthrecord.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicines")
public class MedicineRestController {

    @Autowired
    private MedicineRepository medicineRepository;

    
    @PostMapping("/request")
    public ResponseEntity<?> requestMedicine(@RequestBody Medicine medicine) {
        if (medicineRepository.existsByName(medicine.getName())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên thuốc này đã tồn tại trong hệ thống"));
        }
        medicine.setApproved(false); 
        medicineRepository.save(medicine);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã gửi yêu cầu thêm thuốc. Vui lòng chờ Admin duyệt."));
    }

    
    @GetMapping("/approved")
    public ResponseEntity<List<Medicine>> getApprovedMedicines() {
        return ResponseEntity.ok(medicineRepository.findByIsApprovedTrue());
    }

    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Medicine>> getPendingMedicines() {
        return ResponseEntity.ok(medicineRepository.findByIsApprovedFalse());
    }

    
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveMedicine(@PathVariable Long id) {
        return medicineRepository.findById(id).map(m -> {
            m.setApproved(true);
            medicineRepository.save(m);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã duyệt thuốc thành công"));
        }).orElse(ResponseEntity.notFound().build());
    }

    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMedicine(@PathVariable Long id) {
        medicineRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa thuốc khỏi hệ system"));
    }
}
