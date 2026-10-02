package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.Vaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VaccinationRepository extends JpaRepository<Vaccination, Long> {

    List<Vaccination> findByHealthProfileOrderByVaccinationDateDesc(HealthProfile healthProfile);

    long countByHealthProfile(HealthProfile healthProfile);
}