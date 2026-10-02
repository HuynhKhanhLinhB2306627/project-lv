package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.BodyMetric;
import com.healthrecord.healthrecord.entity.HealthProfile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BodyMetricRepository extends JpaRepository<BodyMetric, Long> {

    List<BodyMetric> findByHealthProfileOrderByLogDateDesc(HealthProfile healthProfile);

    
    @Query("SELECT bm FROM BodyMetric bm WHERE bm.healthProfile.id = :profileId ORDER BY bm.logDate DESC, bm.id DESC")
    List<BodyMetric> findLatestByProfileId(@Param("profileId") Long profileId, Pageable pageable);

    
    default Optional<BodyMetric> findLatestByProfileId(Long profileId) {
        return findLatestByProfileId(profileId, PageRequest.of(0, 1)).stream().findFirst();
    }
}