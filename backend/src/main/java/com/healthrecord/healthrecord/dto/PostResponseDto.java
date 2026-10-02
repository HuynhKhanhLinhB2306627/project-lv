package com.healthrecord.healthrecord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private String author;
    private Long authorId;
    private String category;
    private String media;
    private int likes;
    private int views;
    private String date; 
    
    @JsonProperty("isLiked")
    private boolean isLiked;

    @JsonProperty("isSaved")
    private boolean isSaved;

    @JsonProperty("isPublic")
    private boolean isPublic;
    
    private String status;
    private int commentsCount;
    private java.util.List<String> likedByNames;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getMedia() { return media; }
    public void setMedia(String media) { this.media = media; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    @JsonProperty("isLiked")
    public boolean getIsLiked() { return isLiked; }
    
    @JsonProperty("isLiked")
    public void setIsLiked(boolean isLiked) { this.isLiked = isLiked; }

    @JsonProperty("isSaved")
    public boolean getIsSaved() { return isSaved; }

    @JsonProperty("isSaved")
    public void setIsSaved(boolean isSaved) { this.isSaved = isSaved; }

    @JsonProperty("isPublic")
    public boolean getIsPublic() { return isPublic; }

    @JsonProperty("isPublic")
    public void setIsPublic(boolean isPublic) { this.isPublic = isPublic; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public int getCommentsCount() { return commentsCount; }
    public void setCommentsCount(int commentsCount) { this.commentsCount = commentsCount; }

    public java.util.List<String> getLikedByNames() { return likedByNames; }
    public void setLikedByNames(java.util.List<String> likedByNames) { this.likedByNames = likedByNames; }
}
