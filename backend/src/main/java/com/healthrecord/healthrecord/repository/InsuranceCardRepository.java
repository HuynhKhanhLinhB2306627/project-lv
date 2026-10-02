package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.HealthProfile;
import com.healthrecord.healthrecord.entity.InsuranceCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceCardRepository extends JpaRepository<InsuranceCard, Long> {

    List<InsuranceCard> findByHealthProfileOrderByExpiryDateDesc(HealthProfile healthProfile);

    long countByHealthProfile(HealthProfile healthProfile);
}