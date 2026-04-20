package com.example.week12ChatwithDocs.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentProcessingService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter;

    @Autowired
    public DocumentProcessingService(VectorStore vectorStore, TokenTextSplitter tokenTextSplitter) {
        this.vectorStore = vectorStore;
        this.tokenTextSplitter = tokenTextSplitter;
    }

    /**
     * Process a document file: load, chunk, and store in vector database
     */
    public int processDocument(MultipartFile file) throws IOException {
        // Step 1: Load the document
        List<Document> documents = new ArrayList<>(loadDocument(file));
        if (documents.isEmpty()) {
            throw new IllegalArgumentException("No readable content found in uploaded document");
        }

        // Step 2: Add metadata to documents
        String parentDocId = UUID.randomUUID().toString();
        List<Document> sanitizedDocuments = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
            metadata.put("filename", file.getOriginalFilename());
            metadata.put("upload_timestamp", LocalDateTime.now().toString());
            metadata.put("page_number", i + 1);
            metadata.put("source", file.getOriginalFilename());
            metadata.put("parent_document_id", parentDocId);
            
            String text = sanitizeExtractedText(doc.getText());
            if (!text.isBlank()) {
                sanitizedDocuments.add(new Document(text, metadata));
            }
        }

        if (sanitizedDocuments.isEmpty()) {
            throw new IllegalArgumentException("No readable text could be extracted from the uploaded file");
        }
        
        // Step 3: Chunk the documents
        List<Document> chunks = chunkDocuments(sanitizedDocuments, parentDocId);
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("Document did not produce valid text chunks for indexing");
        }

        // Step 4: Store in vector database
        storeDocuments(chunks);
        
        return chunks.size();
    }

    /**
     * Load document using appropriate reader based on file type
     */
    private List<Document> loadDocument(MultipartFile file) throws IOException {
        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        // Use PDF reader for PDF files
        if ("application/pdf".equals(contentType) || 
            (filename != null && filename.toLowerCase().endsWith(".pdf"))) {
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource);
            return pdfReader.get();
        }
        
        // Use Tika reader for other formats (DOCX, TXT, etc.)
        TikaDocumentReader tikaReader = new TikaDocumentReader(resource);
        return tikaReader.get();
    }

    /**
     * Chunk documents using TokenTextSplitter
     */
    private List<Document> chunkDocuments(List<Document> documents, String parentDocId) {
        List<Document> chunks = new ArrayList<>(tokenTextSplitter.apply(documents));
        if (chunks.isEmpty()) {
            return Collections.emptyList();
        }

        List<Document> sanitizedChunks = new ArrayList<>();

        // Add chunk-specific metadata
        for (int i = 0; i < chunks.size(); i++) {
            Document chunk = chunks.get(i);
            Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
            metadata.put("chunk_index", i);
            metadata.put("total_chunks", chunks.size());
            metadata.put("parent_document_id", parentDocId);
            
            String text = sanitizeExtractedText(chunk.getText());
            if (!text.isBlank()) {
                sanitizedChunks.add(new Document(text, metadata));
            }
        }
        
        return sanitizedChunks;
    }

    /**
     * Remove null bytes and trim extracted content before DB/vector operations.
     */
    private String sanitizeExtractedText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return text
                .replace("\u0000", "")
                .trim();
    }

    /**
     * Store documents in vector database
     */
    private void storeDocuments(List<Document> documents) {
        vectorStore.add(documents);
    }

    /**
     * Check if file type is supported
     */
    public boolean isSupportedFileType(String contentType, String filename) {
        if (contentType != null) {
            if (contentType.equals("application/pdf") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("application/msword") ||
                contentType.equals("text/plain")) {
                return true;
            }
        }
        
        if (filename != null) {
            String lowerFilename = filename.toLowerCase();
            return lowerFilename.endsWith(".pdf") ||
                   lowerFilename.endsWith(".docx") ||
                   lowerFilename.endsWith(".doc") ||
                   lowerFilename.endsWith(".txt");
        }
        
        return false;
    }
}
