# Prompt Template Guide

## Overview

The RAG service uses an external prompt template file for better maintainability and easier prompt engineering. This allows you to modify the prompt without changing Java code.

## Template Location

**File:** `src/main/resources/prompts/rag-prompt-template.st`

## Current Template

```
You are a helpful assistant that answers questions based on the provided context.

Context:
{context}

Question: {query}

Answer the question based only on the context provided above. If the context doesn't contain enough information to answer the question, say "I don't have enough information to answer that question based on the available documents."

Answer:
```

## Template Variables

- `{context}` - Aggregated text from retrieved document chunks
- `{query}` - User's question

## How It Works

1. **Loading**: RagService loads the template at startup using Spring's `@Value` and `Resource`
2. **Variable Substitution**: Spring AI's `PromptTemplate` replaces `{context}` and `{query}` with actual values
3. **LLM Call**: The filled template is sent to OpenAI's chat model

## Customizing the Prompt

### Example 1: More Detailed Answers

```
You are an expert assistant that provides comprehensive answers based on the provided context.

Context:
{context}

Question: {query}

Instructions:
1. Analyze the context carefully
2. Provide a detailed answer with specific examples from the context
3. If the context is insufficient, explain what information is missing

Answer:
```

### Example 2: Concise Answers

```
You are a concise assistant. Answer briefly based on the context.

Context:
{context}

Question: {query}

Provide a brief, direct answer. If context is insufficient, say "Insufficient information."

Answer:
```

### Example 3: Structured Answers

```
You are a helpful assistant. Answer questions using the provided context.

Context:
{context}

Question: {query}

Format your answer as:
- **Summary**: Brief answer
- **Details**: Supporting information from context
- **Sources**: Mention which parts of the context were most relevant

If insufficient information, state clearly.

Answer:
```

## Testing After Changes

After modifying the prompt template:

1. **Restart the application** (Spring DevTools will auto-restart)
2. **Test with curl**:
   ```bash
   curl -X POST http://localhost:8080/api/chat \
     -H "Content-Type: application/json" \
     -d '{"query": "Test question", "topK": 3}'
   ```
3. **Verify the response format** matches your expectations

## Best Practices

1. **Be Specific**: Clearly instruct the LLM on how to use the context
2. **Handle Edge Cases**: Include instructions for insufficient information
3. **Format Guidance**: If you want structured output, specify the format
4. **Test Iteratively**: Make small changes and test frequently
5. **Version Control**: Keep different versions for A/B testing

## Advanced: Multiple Templates

You can create multiple templates for different use cases:

```
prompts/
  rag-prompt-template.st          # Default
  rag-detailed-prompt.st          # Detailed answers
  rag-concise-prompt.st           # Brief answers
  rag-technical-prompt.st         # Technical documentation
```

Then modify RagService to select templates based on request parameters.
