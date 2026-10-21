package com.healthrecord.healthrecord.dto;

public class AiSuggestResponse {
    private String suggestion;

    public AiSuggestResponse() {}

    public AiSuggestResponse(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
}
