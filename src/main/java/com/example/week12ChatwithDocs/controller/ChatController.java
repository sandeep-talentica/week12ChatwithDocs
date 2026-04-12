package com.example.week12ChatwithDocs.controller;

import com.example.week12ChatwithDocs.dto.ChatRequest;
import com.example.week12ChatwithDocs.dto.ChatResponse;
import com.example.week12ChatwithDocs.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final RagService ragService;

    @Autowired
    public ChatController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * Chat endpoint - ask questions about your documents
     * 
     * @param request ChatRequest with query and optional topK
     * @return ChatResponse with answer, sources, and metadata
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            logger.info("Received chat request: {}", request.query());

            // Validate request
            if (request.query() == null || request.query().isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(ChatResponse.error("Query cannot be empty"));
            }

            // Process RAG request
            Map<String, Object> result = ragService.chat(request.query(), request.getTopK());

            // Build response
            ChatResponse response = ChatResponse.success(
                    (String) result.get("answer"),
                    (List<String>) result.get("sources"),
                    (Integer) result.get("chunksUsed"));

            logger.info("Chat request processed successfully. Chunks used: {}", result.get("chunksUsed"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing chat request", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ChatResponse.error("Error processing request: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chat API is running");
    }
}
