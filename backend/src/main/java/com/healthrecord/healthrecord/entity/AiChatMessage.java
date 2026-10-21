package com.healthrecord.healthrecord.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_chat_messages")
public class AiChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private AiChatSession session;

    @Enumerated(EnumType.STRING)
    private Role role; 

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role { USER, BOT }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AiChatSession getSession() { return session; }
    public void setSession(AiChatSession session) { this.session = session; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
