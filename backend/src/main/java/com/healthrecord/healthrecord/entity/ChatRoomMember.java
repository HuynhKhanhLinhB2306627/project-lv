package com.healthrecord.healthrecord.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room_members")
public class ChatRoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime joinedAt;

    
    @Column(name = "is_admin", nullable = false)
    private Boolean isAdmin = false; 
    

    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
        
        if (this.isAdmin == null) {
            this.isAdmin = false;
        }
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ChatRoom getChatRoom() { return chatRoom; }
    public void setChatRoom(ChatRoom chatRoom) { this.chatRoom = chatRoom; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    
    public Boolean getIsAdmin() { return isAdmin; }
    public void setIsAdmin(Boolean admin) { isAdmin = admin; }
}