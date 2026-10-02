package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthProfileRepository extends JpaRepository<HealthProfile, Long> {
    
    List<HealthProfile> findByUser(User user);
    List<HealthProfile> findByUserId(Long userId);
}