package com.healthrecord.healthrecord.service;

import com.healthrecord.healthrecord.dto.ChatMessageDto;
import com.healthrecord.healthrecord.dto.ChatRoomDto;
import com.healthrecord.healthrecord.entity.*;
import com.healthrecord.healthrecord.repository.*;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private ChatRoomMemberRepository chatRoomMemberRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BlockListRepository blockListRepository;

    @Autowired private com.healthrecord.healthrecord.repository.NotificationRepository notificationRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    public List<ChatRoomDto> getMyRoomDtos() {
        User currentUser = SecurityUtils.getCurrentUser();
        List<ChatRoomMember> memberships = chatRoomMemberRepository.findByUser(currentUser);

        List<ChatRoomDto> dtos = new ArrayList<>();
        for (ChatRoomMember member : memberships) {
            dtos.add(convertToDto(member.getChatRoom(), currentUser));
        }
        return dtos;
    }

    public ChatRoomDto getRoomDto(Long roomId) {
        User currentUser = SecurityUtils.getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);
        if (room == null || chatRoomMemberRepository.findByChatRoomAndUser(room, currentUser).isEmpty()) {
            return null;
        }
        return convertToDto(room, currentUser);
    }

    private ChatRoomDto convertToDto(ChatRoom room, User currentUser) {
        String displayName = room.getName();
        Long targetId = null;
        String avatar = null;
        boolean blockedByMe = false;
        boolean blockedByTarget = false;

        if (room.getType() == ChatRoom.RoomType.PRIVATE) {
            User target = room.getMembers().stream()
                    .map(ChatRoomMember::getUser)
                    .filter(u -> !u.getId().equals(currentUser.getId()))
                    .findFirst()
                    .orElse(null);

            if (target != null) {
                displayName = target.getFullName();
                targetId = target.getId();
                avatar = getUserAvatar(target);
                
                
                blockedByMe = blockListRepository.existsByBlockerAndBlocked(currentUser, target);
                blockedByTarget = blockListRepository.existsByBlockerAndBlocked(target, currentUser);
            }
        }
        
        ChatRoomDto dto = new ChatRoomDto(room.getId(), displayName, targetId, avatar);
        dto.setBlockedByMe(blockedByMe);
        dto.setBlockedByTarget(blockedByTarget);
        return dto;
    }

    public List<ChatMessageDto> getRoomHistory(Long roomId) {
        User currentUser = SecurityUtils.getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);

        if (room == null || chatRoomMemberRepository.findByChatRoomAndUser(room, currentUser).isEmpty()) {
            return new ArrayList<>();
        }

        List<Message> messages = messageRepository.findByChatRoomOrderBySentAtAsc(room);

        return messages.stream().map(msg -> {
            String timeStr = (msg.getSentAt() != null) ?
                    msg.getSentAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "Unknown";

            String senderName = "Unknown";
            String senderEmail = "";
            String avatar = null;
            if (msg.getSender() != null) {
                senderName = msg.getSender().getFullName();
                senderEmail = msg.getSender().getEmail();
                avatar = getUserAvatar(msg.getSender());
            }

            return new ChatMessageDto(
                    msg.getId(),
                    msg.getContent(),
                    senderName,
                    senderEmail,
                    avatar,
                    timeStr,
                    ChatMessageDto.MessageType.CHAT
            );
        }).collect(Collectors.toList());
    }

    public ChatMessageDto sendAndConvertMessage(Long roomId, String content, String senderEmail) {
        Message savedMsg = saveMessage(roomId, content, senderEmail);
        String timeStr = (savedMsg.getSentAt() != null) 
                ? savedMsg.getSentAt().format(DateTimeFormatter.ofPattern("HH:mm")) 
                : "Now";
        
        User sender = savedMsg.getSender();
        String avatar = getUserAvatar(sender);

        return new ChatMessageDto(
                savedMsg.getId(),
                savedMsg.getContent(),
                sender.getFullName(),
                sender.getEmail(),
                avatar,
                timeStr,
                ChatMessageDto.MessageType.CHAT
        );
    }

    public String getUserAvatar(User user) {
        if (user == null) return null;
        try {
            
            User managedUser = userRepository.findById(user.getId()).orElse(null);
            if (managedUser == null || managedUser.getHealthProfiles() == null || managedUser.getHealthProfiles().isEmpty()) {
                return null;
            }
            
            return managedUser.getHealthProfiles().get(0).getAvatar();
        } catch (Exception e) {
            return null;
        }
    }

    public Message saveMessage(Long roomId, String content, String senderUsername) {
        User currentUser = userRepository.findByEmail(senderUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();

        if (chatRoomMemberRepository.findByChatRoomAndUser(room, currentUser).isEmpty()) {
            throw new SecurityException("Bạn không phải thành viên của phòng chat này");
        }

        User receiver = room.getMembers().stream()
                .map(ChatRoomMember::getUser)
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .findFirst()
                .orElse(currentUser);

        
        if (blockListRepository.existsByBlockerAndBlocked(receiver, currentUser)) {
            throw new RuntimeException("BLOCKED_BY_USER");
        }

        
        if (blockListRepository.existsByBlockerAndBlocked(currentUser, receiver)) {
            throw new RuntimeException("YOU_BLOCKED_THEM");
        }

        Message msg = new Message();
        msg.setChatRoom(room);
        msg.setSender(currentUser);
        msg.setReceiver(receiver);
        msg.setContent(content);

        Message saved = messageRepository.save(msg);

        
        if (!receiver.getId().equals(currentUser.getId())) {
            notificationService.createAndSendNotification(receiver, currentUser.getFullName() + " đã gửi tin nhắn cho bạn.", "/chat?roomId=" + roomId);
        }

        return saved;
    }

    public Long deleteOwnMessage(Long roomId, Long messageId, String requesterEmail) {
        User currentUser = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
        if (chatRoomMemberRepository.findByChatRoomAndUser(room, currentUser).isEmpty()) {
            throw new SecurityException("Bạn không phải thành viên của phòng chat này");
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("MESSAGE_NOT_FOUND"));

        if (!message.getChatRoom().getId().equals(roomId)) {
            throw new SecurityException("MESSAGE_NOT_IN_ROOM");
        }

        if (message.getSender() == null || !message.getSender().getId().equals(currentUser.getId())) {
            throw new RuntimeException("DELETE_NOT_ALLOWED");
        }

        messageRepository.delete(message);
        return messageId;
    }

    public ChatRoom getOrCreatePrivateRoom(Long targetUserId) {
        User currentUser = SecurityUtils.getCurrentUser();
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<ChatRoomMember> memberships = chatRoomMemberRepository.findByUser(currentUser);
        for (ChatRoomMember member : memberships) {
            ChatRoom room = member.getChatRoom();
            if (room.getType() == ChatRoom.RoomType.PRIVATE) {
                boolean hasTarget = room.getMembers().stream()
                        .anyMatch(m -> m.getUser().getId().equals(targetUserId));
                if (hasTarget) return room;
            }
        }

        ChatRoom room = new ChatRoom();
        room.setName(targetUser.getFullName());
        room.setType(ChatRoom.RoomType.PRIVATE);
        chatRoomRepository.save(room);

        addMemberToRoom(room, currentUser);
        addMemberToRoom(room, targetUser);
        return room;
    }

    private void addMemberToRoom(ChatRoom room, User user) {
        ChatRoomMember member = new ChatRoomMember();
        member.setChatRoom(room);
        member.setUser(user);
        chatRoomMemberRepository.save(member);
    }

    public boolean isBlocked(Long userId) {
        User me = SecurityUtils.getCurrentUser();
        User target = userRepository.findById(userId).orElseThrow();
        
        return blockListRepository.existsByBlockerAndBlocked(me, target);
    }

    public void blockUser(Long targetUserId) {
        User me = SecurityUtils.getCurrentUser();
        User target = userRepository.findById(targetUserId).orElseThrow();
        if (!blockListRepository.existsByBlockerAndBlocked(me, target)) {
            blockListRepository.save(new BlockList(me, target));
            
            
            notifyBlockStatusChange(me, target, true);
        }
    }

    public void unblockUser(Long targetUserId) {
        User me = SecurityUtils.getCurrentUser();
        User target = userRepository.findById(targetUserId).orElseThrow();
        blockListRepository.findByBlockerAndBlocked(me, target)
                .ifPresent(blockListRepository::delete);
        
        
        notifyBlockStatusChange(me, target, false);
    }

    private void notifyBlockStatusChange(User blocker, User blocked, boolean isBlocked) {
        
        List<ChatRoomMember> memberships = chatRoomMemberRepository.findByUser(blocker);
        for (ChatRoomMember member : memberships) {
            ChatRoom room = member.getChatRoom();
            if (room.getType() == ChatRoom.RoomType.PRIVATE) {
                boolean hasTarget = room.getMembers().stream()
                        .anyMatch(m -> m.getUser().getId().equals(blocked.getId()));
                if (hasTarget) {
                    ChatMessageDto statusMsg = new ChatMessageDto();
                    statusMsg.setType(ChatMessageDto.MessageType.SYSTEM);
                    statusMsg.setContent(isBlocked ? "BLOCK" : "UNBLOCK");
                    statusMsg.setSenderEmail(blocker.getEmail());
                    
                    messagingTemplate.convertAndSend("/topic/messages/" + room.getId(), statusMsg);
                    break;
                }
            }
        }
    }
}
