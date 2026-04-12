package com.example.week12ChatwithDocs.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ChatResponse(
        boolean success,
        String answer,
        List<String> sources,
        int chunksUsed,
        LocalDateTime timestamp,
        String errorMessage) {
    public static ChatResponse success(String answer, List<String> sources, int chunksUsed) {
        return new ChatResponse(
                true,
                answer,
                sources,
                chunksUsed,
                LocalDateTime.now(),
                null);
    }

    public static ChatResponse error(String errorMessage) {
        return new ChatResponse(
                false,
                null,
                List.of(),
                0,
                LocalDateTime.now(),
                errorMessage);
    }
}
