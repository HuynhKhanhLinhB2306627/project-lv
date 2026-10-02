package com.healthrecord.healthrecord.dto;

public class ChatMessageDto {
    private Long id;            
    private String content;
    private String sender;      
    private String senderEmail; 
    private String avatar;      
    private String time;        
    private MessageType type;
    private String tempId;      

    public enum MessageType {
        CHAT, JOIN, LEAVE, SYSTEM, ERROR
    }

    
    public ChatMessageDto() {}

    
    public ChatMessageDto(String content, String sender, String senderEmail, String avatar, String time, MessageType type) {
        this.content = content;
        this.sender = sender;
        this.senderEmail = senderEmail;
        this.avatar = avatar;
        this.time = time;
        this.type = type;
    }

    
    public ChatMessageDto(Long id, String content, String sender, String senderEmail, String avatar, String time, MessageType type) {
        this.id = id;
        this.content = content;
        this.sender = sender;
        this.senderEmail = senderEmail;
        this.avatar = avatar;
        this.time = time;
        this.type = type;
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public String getTempId() { return tempId; }
    public void setTempId(String tempId) { this.tempId = tempId; }
}
