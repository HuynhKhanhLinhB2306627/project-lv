package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.MedicationCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicationCourseRepository extends JpaRepository<MedicationCourse, Long> {

    List<MedicationCourse> findByHealthProfileOrderByStartDateDesc(HealthProfile healthProfile);

    long countByHealthProfile(HealthProfile healthProfile);
}