package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.BlockList;
import com.healthrecord.healthrecord.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BlockListRepository extends JpaRepository<BlockList, Long> {
    boolean existsByBlockerAndBlocked(User blocker, User blocked);
    Optional<BlockList> findByBlockerAndBlocked(User blocker, User blocked);
}