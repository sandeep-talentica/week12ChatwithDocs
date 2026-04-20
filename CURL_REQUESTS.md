# cURL Requests for All Controller Endpoints

Use these commands directly in terminal, or paste them into Postman/Bruno using **Import -> Raw Text (cURL)**.

## 1) Upload a document

```bash
curl -X POST "http://localhost:8080/api/documents/upload" -F "file=@/absolute/path/to/your-document.pdf"
```

## 2) Document API health check

```bash
curl -X GET "http://localhost:8080/api/documents/health"
```

## 3) Chat with documents

```bash
curl -X POST "http://localhost:8080/api/chat" -H "Content-Type: application/json" -d '{"query":"Summarize the uploaded document","topK":5}'
```

## 4) Chat API health check

```bash
curl -X GET "http://localhost:8080/api/chat/health"
```

## Optional: use a BASE_URL variable

```bash
BASE_URL="http://localhost:8080"
curl -X GET "$BASE_URL/api/documents/health"
```

