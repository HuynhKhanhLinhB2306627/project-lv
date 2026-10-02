package com.healthrecord.healthrecord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatRoomDto {
    private Long id;
    private String name;
    private Long targetUserId;
    private String avatar;
    private String avatarChar;
    
    @JsonProperty("blockedByMe")
    private boolean blockedByMe;
    
    @JsonProperty("blockedByTarget")
    private boolean blockedByTarget;

    public ChatRoomDto(Long id, String name, Long targetUserId, String avatar) {
        this.id = id;
        this.name = name;
        this.targetUserId = targetUserId;
        this.avatar = avatar;
        this.avatarChar = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "?";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getAvatarChar() { return avatarChar; }
    public void setAvatarChar(String avatarChar) { this.avatarChar = avatarChar; }

    public boolean isBlockedByMe() { return blockedByMe; }
    public void setBlockedByMe(boolean blockedByMe) { this.blockedByMe = blockedByMe; }
    public boolean isBlockedByTarget() { return blockedByTarget; }
    public void setBlockedByTarget(boolean blockedByTarget) { this.blockedByTarget = blockedByTarget; }
}
