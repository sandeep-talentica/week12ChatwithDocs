package com.example.week12ChatwithDocs.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final String promptTemplateContent;

    @Value("${rag.top-k:5}")
    private int defaultTopK;

    public RagService(
            VectorStore vectorStore,
            ChatClient.Builder chatClientBuilder,
            @Value("classpath:/prompts/rag-prompt-template.st") Resource promptResource) throws IOException {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
        this.promptTemplateContent = promptResource.getContentAsString(StandardCharsets.UTF_8);
    }

    /**
     * Main RAG workflow: retrieve context and generate answer
     */
    public Map<String, Object> chat(String query, int topK) {
        // Step 1: Retrieve relevant document chunks from vector store
        List<Document> relevantDocs = retrieveContext(query, topK);

        if (relevantDocs.isEmpty()) {
            return Map.of(
                    "answer", "No relevant documents found to answer your question.",
                    "sources", List.of(),
                    "chunksUsed", 0);
        }

        // Step 2: Extract text content and sources
        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        List<String> sources = relevantDocs.stream()
                .map(doc -> doc.getMetadata().getOrDefault("filename", "unknown").toString())
                .distinct()
                .collect(Collectors.toList());

        // Step 3: Generate answer using prompt template
        String answer = generateAnswer(query, context);

        return Map.of(
                "answer", answer,
                "sources", sources,
                "chunksUsed", relevantDocs.size());
    }

    /**
     * Retrieve relevant document chunks from vector store
     */
    private List<Document> retrieveContext(String query, int topK) {
        // Use simple similarity search and limit results
        return vectorStore.similaritySearch(query).stream()
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * Generate answer using LLM with context stuffing
     */
    private String generateAnswer(String query, String context) {
        PromptTemplate promptTemplate = new PromptTemplate(promptTemplateContent);

        Map<String, Object> model = Map.of(
                "context", context,
                "query", query);

        return chatClient.prompt()
                .user(promptTemplate.create(model).getContents())
                .call()
                .content();
    }
}
