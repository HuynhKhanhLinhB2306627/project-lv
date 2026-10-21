package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.AiChatSession;
import com.healthrecord.healthrecord.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiChatSessionRepository extends JpaRepository<AiChatSession, Long> {
    List<AiChatSession> findByUserOrderByCreatedAtDesc(User user);
}
