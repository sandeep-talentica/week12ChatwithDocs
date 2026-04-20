package com.example.week12ChatwithDocs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.week12ChatwithDocs.dto.DocumentUploadResponse;
import com.example.week12ChatwithDocs.service.DocumentProcessingService;

@RestController
@RequestMapping("/api/documents")
public class DocumentProcessingController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingController.class);
    private final DocumentProcessingService documentProcessingService;

    @Autowired
    public DocumentProcessingController(DocumentProcessingService documentProcessingService) {
        this.documentProcessingService = documentProcessingService;
    }

    /**
     * Upload and process a document file
     * 
     * @param file The document file to process (PDF, DOCX, TXT, etc.)
     * @return Response with processing status and chunk count
     */
    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file) {

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(DocumentUploadResponse.error(
                                file.getOriginalFilename(),
                                "File is empty"));
            }

            // Check file type
            if (!documentProcessingService.isSupportedFileType(
                    file.getContentType(),
                    file.getOriginalFilename())) {
                return ResponseEntity
                        .badRequest()
                        .body(DocumentUploadResponse.error(
                                file.getOriginalFilename(),
                                "Unsupported file type. Supported types: PDF, DOCX, DOC, TXT"));
            }

            // Process the document
            int chunksCreated = documentProcessingService.processDocument(file);

            // Return success response
            return ResponseEntity
                    .ok()
                    .body(DocumentUploadResponse.success(
                            file.getOriginalFilename(),
                            chunksCreated));

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid document upload: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(DocumentUploadResponse.error(
                            file.getOriginalFilename(),
                            e.getMessage()));
        } catch (Exception e) {
            // Log the full exception for debugging
            logger.error("Error processing document: " + file.getOriginalFilename(), e);

            // Handle any processing errors
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DocumentUploadResponse.error(
                            file.getOriginalFilename(),
                            "Error processing document: " + errorMsg));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Document Processing API is running");
    }
}
