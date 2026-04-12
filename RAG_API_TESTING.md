# RAG API Testing Guide

## Quick Start

### 1. Start the Application
```bash
cd /Users/vikasbharti/Documents/ai-engineering-workspace/week12ChatwithDocs
./gradlew bootRun
```

### 2. Test the Health Endpoint
```bash
curl http://localhost:8080/api/chat/health
```

Expected: `Chat API is running`

### 3. Ask a Question
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "query": "What is the main topic of the documents?",
    "topK": 3
  }'
```

### 4. More Example Queries

**General Question:**
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"query": "Tell me about Spring AI", "topK": 5}'
```

**Specific Topic:**
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"query": "How does document processing work?", "topK": 3}'
```

**Technical Details:**
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"query": "What are the key features?", "topK": 4}'
```

## Response Format

```json
{
  "success": true,
  "answer": "Generated answer based on retrieved context",
  "sources": ["document1.pdf", "document2.txt"],
  "chunksUsed": 3,
  "timestamp": "2026-01-25T09:54:27.490754",
  "errorMessage": null
}
```

## Configuration

Edit `application.properties` to customize:
```properties
# Number of chunks to retrieve
rag.top-k=5

# Similarity threshold (0.0 to 1.0)
rag.similarity-threshold=0.7

# OpenAI model
spring.ai.openai.chat.options.model=gpt-4o-mini

# Temperature (0.0 to 1.0)
spring.ai.openai.chat.options.temperature=0.7
```

## Troubleshooting

**Port already in use:**
- Make sure no other application is running on port 8080
- Or change the port in `application.properties`: `server.port=8081`

**No documents found:**
- Ensure documents are uploaded to the vector store first
- Check database connection in `application.properties`

**API errors:**
- Check OpenAI API key in `.env` file
- Verify database is running: `docker ps`
