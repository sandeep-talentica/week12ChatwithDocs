# Week12 Chat with Docs - Complete API Guide

## Overview

This application provides a complete RAG (Retrieval-Augmented Generation) system with two main functionalities:
1. **Document Upload** - Upload and process documents into the vector database
2. **Chat with Docs** - Ask questions about your uploaded documents

## Available Endpoints

### 1. Document Upload API

**Upload Document**
```bash
POST /api/documents/upload
```

Upload a document file (PDF, DOCX, TXT) to be processed and stored in the vector database.

**Example:**
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@/path/to/your/document.pdf"
```

**Response:**
```json
{
  "success": true,
  "filename": "document.pdf",
  "chunksCreated": 5,
  "timestamp": "2026-01-25T10:13:00.000000",
  "message": "Document processed and stored successfully",
  "errorMessage": null
}
```

**Health Check:**
```bash
curl http://localhost:8080/api/documents/health
```

---

### 2. Chat API (RAG)

**Ask Questions**
```bash
POST /api/chat
```

Ask questions about your uploaded documents. The system retrieves relevant chunks and generates contextual answers.

**Example:**
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "query": "What is Spring AI?",
    "topK": 3
  }'
```

**Response:**
```json
{
  "success": true,
  "answer": "Spring AI is a document processing system that utilizes advanced natural language processing techniques...",
  "sources": ["Spring AI Document Processing System.docx", "sample-document.txt"],
  "chunksUsed": 3,
  "timestamp": "2026-01-25T10:13:15.012262",
  "errorMessage": null
}
```

**Health Check:**
```bash
curl http://localhost:8080/api/chat/health
```

---

## Complete Workflow

### Step 1: Upload Documents

```bash
# Upload a PDF
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@document.pdf"

# Upload a text file
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@notes.txt"

# Upload a Word document
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@report.docx"
```

### Step 2: Chat with Your Documents

```bash
# General question
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"query": "What are the main topics covered?", "topK": 5}'

# Specific question
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"query": "Explain the architecture", "topK": 3}'

# Technical question
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"query": "How does chunking work?", "topK": 4}'
```

---

## Features

### Document Processing
- ✅ Multi-format support (PDF, DOCX, TXT)
- ✅ Intelligent chunking with TokenTextSplitter
- ✅ Automatic metadata enrichment
- ✅ Vector embedding generation
- ✅ PgVector database storage

### RAG Chat
- ✅ Semantic search across documents
- ✅ Context-aware answer generation
- ✅ Source attribution
- ✅ Configurable retrieval (topK parameter)
- ✅ External prompt template for easy customization

---

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Document Processing
document.processing.chunk-size=800
document.processing.chunk-overlap=100

# RAG Settings
rag.top-k=5
rag.similarity-threshold=0.7

# OpenAI
spring.ai.openai.chat.options.model=gpt-4o-mini
spring.ai.openai.chat.options.temperature=0.7
spring.ai.openai.embedding.options.model=text-embedding-3-small
```

---

## Prompt Template

Customize the RAG prompt at:
`src/main/resources/prompts/rag-prompt-template.st`

See `PROMPT_TEMPLATE_GUIDE.md` for examples and best practices.

---

## Running the Application

```bash
# Start the application
./gradlew bootRun

# The application will start on port 8080
# API endpoints will be available at http://localhost:8080
```

---

## Testing

```bash
# Test document upload health
curl http://localhost:8080/api/documents/health

# Test chat health
curl http://localhost:8080/api/chat/health

# Upload a test document
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@sample-document.txt"

# Ask a question
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"query": "Test question", "topK": 3}'
```

---

## Architecture

```
User Request
    ↓
┌─────────────────────────────────────┐
│  Document Upload Flow               │
├─────────────────────────────────────┤
│ 1. Upload File (PDF/DOCX/TXT)      │
│ 2. DocumentProcessingService        │
│    - Load with PDF/Tika Reader      │
│    - Chunk with TokenTextSplitter   │
│    - Add metadata                   │
│ 3. Store in PgVector                │
└─────────────────────────────────────┘

User Query
    ↓
┌─────────────────────────────────────┐
│  RAG Chat Flow                      │
├─────────────────────────────────────┤
│ 1. RagService.chat()                │
│ 2. Vector similarity search         │
│ 3. Retrieve top K chunks            │
│ 4. Load prompt template             │
│ 5. Stuff context into prompt        │
│ 6. Call OpenAI ChatClient           │
│ 7. Return answer with sources       │
└─────────────────────────────────────┘
```

---

## Error Handling

Both APIs include comprehensive error handling:

**Document Upload Errors:**
- Empty file
- Unsupported file type
- Processing errors

**Chat Errors:**
- Empty query
- No relevant documents found
- LLM errors

All errors return structured JSON responses with appropriate HTTP status codes.
