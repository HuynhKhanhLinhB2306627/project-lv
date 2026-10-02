package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.ChatRoom;
import com.healthrecord.healthrecord.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByChatRoomOrderBySentAtAsc(ChatRoom chatRoom);
}