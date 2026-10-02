package com.healthrecord.healthrecord.controller.api;

import com.healthrecord.healthrecord.dto.MedicineDto;
import com.healthrecord.healthrecord.entity.Medicine;
import com.healthrecord.healthrecord.entity.Post;
import com.healthrecord.healthrecord.entity.PostReport;
import com.healthrecord.healthrecord.repository.MedicineRepository;
import com.healthrecord.healthrecord.repository.UserRepository;
import com.healthrecord.healthrecord.service.ForumService;
import com.healthrecord.healthrecord.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/posts/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingPosts() {
        List<Post> posts = forumService.getAllPendingPosts();
        return ResponseEntity.ok(posts.stream().map(this::convertPostToAdminMap).collect(Collectors.toList()));
    }

    @GetMapping("/posts/all")
    public ResponseEntity<List<Map<String, Object>>> getAllPosts() {
        List<Post> posts = forumService.getAllApprovedPostsForAdmin(); 
        return ResponseEntity.ok(posts.stream().map(this::convertPostToAdminMap).collect(Collectors.toList()));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<Map<String, Object>>> getReports() {
        List<PostReport> reports = forumService.getAllActiveReports();
        return ResponseEntity.ok(reports.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("reporterName", r.getReporter().getFullName());
            map.put("reason", r.getReason());
            map.put("reportedAt", r.getReportedAt());
            map.put("postId", r.getPost().getId());
            map.put("postTitle", r.getPost().getTitle());
            map.put("postContent", r.getPost().getContent());
            map.put("postAuthor", r.getPost().getAuthor() != null ? r.getPost().getAuthor().getFullName() : "Không rõ");
            map.put("postCreatedAt", r.getPost().getCreatedAt());
            map.put("postMedia", r.getPost().getMediaPath()); 
            return map;
        }).collect(Collectors.toList()));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Post> pendingPosts = forumService.getAllPendingPosts();
        List<Post> approvedPosts = forumService.getAllApprovedPostsForAdmin();
        List<PostReport> activeReports = forumService.getAllActiveReports();
        List<Medicine> allMeds = medicineRepository.findAll();

        long approvedMedicineCount = allMeds.stream().filter(Medicine::isApproved).count();
        long pendingMedicineCount = allMeds.size() - approvedMedicineCount;

        stats.put("totalUsers", userRepository.count());
        stats.put("approvedPosts", approvedPosts.size());
        stats.put("pendingPosts", pendingPosts.size());
        stats.put("activeReports", activeReports.size());
        stats.put("totalMedicines", allMeds.size());
        stats.put("approvedMedicines", approvedMedicineCount);
        stats.put("pendingMedicines", pendingMedicineCount);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/reports/{id}/resolve")
    public ResponseEntity<?> resolveReport(@PathVariable Long id) {
        forumService.resolveReport(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/posts/approve/{id}")
    public ResponseEntity<?> approvePost(@PathVariable Long id) {
        try {
            forumService.approvePost(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        try {
            forumService.deletePost(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/medicines")
    public ResponseEntity<List<Map<String, Object>>> getAllMedicines() {
        
        List<Medicine> medicines = medicineRepository.findAll();
        List<Map<String, Object>> result = medicines.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("medicineName", m.getName());
            map.put("description", m.getDescription());
            map.put("unit", m.getUnit());
            map.put("isApproved", m.isApproved());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/medicines/{id}")
    public ResponseEntity<?> deleteMedicine(@PathVariable Long id) {
        try {
            medicineService.deleteMedicineById(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/medicines/{id}")
    public ResponseEntity<?> updateMedicine(@PathVariable Long id, @RequestBody MedicineDto medicineDto) {
        try {
            if (medicineDto.getName() == null || medicineDto.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tên thuốc không được để trống"));
            }
            medicineDto.setId(id);
            medicineService.saveMedicine(medicineDto);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật thuốc thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/medicines/{id}/approve")
    public ResponseEntity<?> approveMedicine(@PathVariable Long id) {
        try {
            medicineService.approveMedicine(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Duyệt thuốc thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> convertPostToAdminMap(Post p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("title", p.getTitle());
        map.put("content", p.getContent());
        map.put("media", p.getMediaPath());
        map.put("author", p.getAuthor().getFullName());
        map.put("email", p.getAuthor().getEmail());
        map.put("date", p.getCreatedAt());
        map.put("category", p.getCategory() != null ? p.getCategory().getName() : "Chung");
        map.put("status", p.getStatus().name());
        return map;
    }
}
