package com.healthrecord.healthrecord.controller;

import com.healthrecord.healthrecord.dto.ChatMessageDto;
import com.healthrecord.healthrecord.dto.ChatRoomDto;
import com.healthrecord.healthrecord.entity.ChatRoom;
import com.healthrecord.healthrecord.entity.User;
import com.healthrecord.healthrecord.repository.BlockListRepository;
import com.healthrecord.healthrecord.repository.UserRepository;
import com.healthrecord.healthrecord.service.ChatService;
import com.healthrecord.healthrecord.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    @Autowired private ChatService chatService;
    @Autowired private UserRepository userRepository;
    @Autowired private BlockListRepository blockListRepository;

    @GetMapping("/api/chat/rooms")
    @ResponseBody
    public ResponseEntity<?> getMyRooms() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(chatService.getMyRoomDtos());
    }

    @GetMapping("/api/chat/rooms/{roomId}")
    @ResponseBody
    public ResponseEntity<?> getRoomById(@PathVariable Long roomId) {
        ChatRoomDto dto = chatService.getRoomDto(roomId);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/api/chat/history/{roomId}")
    @ResponseBody
    public ResponseEntity<List<ChatMessageDto>> getHistory(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getRoomHistory(roomId));
    }

    @GetMapping("/api/chat/me")
    @ResponseBody
    public ResponseEntity<?> getMyChatInfo() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        Map<String, String> map = new HashMap<>();
        map.put("email", user.getEmail());
        map.put("fullName", user.getFullName());
        
        map.put("avatar", chatService.getUserAvatar(user));
        return ResponseEntity.ok(map);
    }

    @GetMapping("/api/users/search")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> searchUsers(@RequestParam String keyword) {
        User currentUser = SecurityUtils.getCurrentUser();
        List<User> users = userRepository.findByFullNameContainingIgnoreCase(keyword);
        return ResponseEntity.ok(users.stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", u.getId());
                    map.put("fullName", u.getFullName());
                    map.put("email", u.getEmail());
                    map.put("avatar", chatService.getUserAvatar(u));
                    return map;
                }).collect(Collectors.toList()));
    }

    @GetMapping("/api/chat/check-block/{targetUserId}")
    @ResponseBody
    public ResponseEntity<Boolean> checkBlock(@PathVariable Long targetUserId) {
        return ResponseEntity.ok(chatService.isBlocked(targetUserId));
    }

    @PostMapping("/api/chat/block/{targetUserId}")
    @ResponseBody
    public ResponseEntity<?> blockUser(@PathVariable Long targetUserId) {
        chatService.blockUser(targetUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/chat/unblock/{targetUserId}")
    @ResponseBody
    public ResponseEntity<?> unblockUser(@PathVariable Long targetUserId) {
        chatService.unblockUser(targetUserId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/chat/room/with/{targetUserId}")
    @ResponseBody
    public ResponseEntity<ChatRoomDto> getOrCreateRoom(@PathVariable Long targetUserId) {
        ChatRoom room = chatService.getOrCreatePrivateRoom(targetUserId);
        ChatRoomDto dto = chatService.getRoomDto(room.getId());
        return ResponseEntity.ok(dto);
    }

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/messages/{roomId}")
    public ChatMessageDto sendMessage(@Payload ChatMessageDto chatMessageDto, @DestinationVariable Long roomId, Principal principal) {
        if (principal == null) return null;
        try {
            
            ChatMessageDto result = chatService.sendAndConvertMessage(roomId, chatMessageDto.getContent(), principal.getName());
            
            
            if (result != null) {
                result.setTempId(chatMessageDto.getTempId());
            }
            return result;
        } catch (RuntimeException e) {
            
            String errorCode = e.getMessage();
            if ("BLOCKED_BY_USER".equals(errorCode) || "YOU_BLOCKED_THEM".equals(errorCode)) {
                ChatMessageDto errorMsg = new ChatMessageDto();
                errorMsg.setType(ChatMessageDto.MessageType.ERROR);
                errorMsg.setContent(errorCode);
                errorMsg.setTempId(chatMessageDto.getTempId());
                errorMsg.setSenderEmail(principal.getName());
                return errorMsg;
            }
            e.printStackTrace();
            return null; 
        }
    }

    @MessageMapping("/chat.deleteMessage/{roomId}")
    @SendTo("/topic/messages/{roomId}")
    public ChatMessageDto deleteMessage(@Payload ChatMessageDto chatMessageDto, @DestinationVariable Long roomId, Principal principal) {
        if (principal == null || chatMessageDto == null || chatMessageDto.getId() == null) return null;
        try {
            Long deletedId = chatService.deleteOwnMessage(roomId, chatMessageDto.getId(), principal.getName());
            ChatMessageDto result = new ChatMessageDto();
            result.setType(ChatMessageDto.MessageType.SYSTEM);
            result.setContent("DELETE:" + deletedId);
            result.setSenderEmail(principal.getName());
            return result;
        } catch (RuntimeException e) {
            String errorCode = e.getMessage();
            if ("DELETE_NOT_ALLOWED".equals(errorCode) || "MESSAGE_NOT_FOUND".equals(errorCode)) {
                ChatMessageDto errorMsg = new ChatMessageDto();
                errorMsg.setType(ChatMessageDto.MessageType.ERROR);
                errorMsg.setContent(errorCode);
                errorMsg.setSenderEmail(principal.getName());
                errorMsg.setId(chatMessageDto.getId());
                return errorMsg;
            }
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
