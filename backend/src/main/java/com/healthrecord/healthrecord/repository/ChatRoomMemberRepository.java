package com.healthrecord.healthrecord.repository;

import com.healthrecord.healthrecord.entity.ChatRoom;
import com.healthrecord.healthrecord.entity.ChatRoomMember;
import com.healthrecord.healthrecord.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    List<ChatRoomMember> findByUser(User user);
    Optional<ChatRoomMember> findByChatRoomAndUser(ChatRoom chatRoom, User user);
}