package com.example.week12ChatwithDocs.dto;

public record ChatRequest(
        String query,
        Integer topK) {
    public ChatRequest {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }
        // Default topK to 5 if not provided
        if (topK == null) {
            topK = 5;
        }
        if (topK < 1 || topK > 20) {
            throw new IllegalArgumentException("topK must be between 1 and 20");
        }
    }

    public int getTopK() {
        return topK != null ? topK : 5;
    }
}
