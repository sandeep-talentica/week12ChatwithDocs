package com.example.week12ChatwithDocs.dto;

import java.time.LocalDateTime;

public record DocumentUploadResponse(
    boolean success,
    String filename,
    int chunksCreated,
    LocalDateTime timestamp,
    String message,
    String errorMessage
) {
    public static DocumentUploadResponse success(String filename, int chunksCreated) {
        return new DocumentUploadResponse(
            true,
            filename,
            chunksCreated,
            LocalDateTime.now(),
            "Document processed and stored successfully",
            null
        );
    }
    
    public static DocumentUploadResponse error(String filename, String errorMessage) {
        return new DocumentUploadResponse(
            false,
            filename,
            0,
            LocalDateTime.now(),
            "Failed to process document",
            errorMessage
        );
    }
}
