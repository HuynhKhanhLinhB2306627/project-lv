package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.ProfileAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileAccessRepository extends JpaRepository<ProfileAccess, Long> {

    
    List<ProfileAccess> findByHealthProfileId(Long profileId);

    
    Optional<ProfileAccess> findByHealthProfileIdAndUserId(Long profileId, Long userId);

    
    List<ProfileAccess> findByUserId(Long userId);

    
    void deleteByHealthProfileId(Long profileId);
}