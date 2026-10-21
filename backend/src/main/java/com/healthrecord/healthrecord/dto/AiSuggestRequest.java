package com.healthrecord.healthrecord.dto;

public class AiSuggestRequest {
    private String title;
    private String content;

    public AiSuggestRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
