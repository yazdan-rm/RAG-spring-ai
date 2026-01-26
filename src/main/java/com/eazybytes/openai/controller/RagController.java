package com.eazybytes.openai.controller;

import com.eazybytes.openai.exception.InvalidAnswerException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    @Value("classpath:/promptTemplates/systemPromptRandomDataTemplate.st")
    Resource promptTemplate;

    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    Resource hrSystemTemplate;

    @Value("classpath:/promptTemplates/militaryServiceAssistantTemplate.st")
    Resource militaryServiceAssistantPromptTemplate;


    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final FactCheckingEvaluator factCheckingEvaluator;

    public RagController(@Qualifier("chatMemoryChatClient") ChatClient chatClient, VectorStore vectorStore,
                         FactCheckingEvaluator factCheckingEvaluator) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.factCheckingEvaluator = factCheckingEvaluator;
    }


    @GetMapping("/random/chat")
    public ResponseEntity<String> randomChat(@RequestHeader("username") String username,
                                             @RequestParam("message") String message) {
//        SearchRequest searchRequest = SearchRequest.builder().query(message).topK(3).similarityThreshold(0.5
//        ).build();
//        List<Document> similarDocs = vectorStore.similaritySearch(searchRequest);
//        String similarContext = similarDocs.stream()
//                .map(Document::getText)
//                .collect(Collectors.joining(System.lineSeparator()));
        String answer = chatClient.prompt()
//                .system(
//                promptSystemSpec -> promptSystemSpec.text(promptTemplate)
//                        .param("documents", similarContext))
                .advisors(a -> a.param(CONVERSATION_ID, username))
                .user(message)
                .call().content();

        return ResponseEntity.ok(answer);
    }

    @Retryable(retryFor = InvalidAnswerException.class, maxAttempts = 3)
    @GetMapping("/document/chat")
    public ResponseEntity<String> documentChat(@RequestHeader("username") String username,
                                               @RequestParam("message") String message) {
        SearchRequest searchRequest =
                SearchRequest.builder().query(message).topK(4).similarityThreshold(0.5).build();
        List<Document> similarDocs = vectorStore.similaritySearch(searchRequest);
        String similarContext = similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));
        String answer = chatClient.prompt()
                .system(promptSystemSpec -> promptSystemSpec.text(militaryServiceAssistantPromptTemplate)
                        .param("DOCUMENTS", similarContext))
                .advisors(a -> a.param(CONVERSATION_ID, username))
                .user(message)
                .call().content();
//        validateAnswer(message, answer, similarDocs);
        return ResponseEntity.ok(answer);
    }


    private void validateAnswer(String message, String answer, List<Document> similarDocs) {
        EvaluationRequest evaluationRequest =
                new EvaluationRequest(message, similarDocs, answer);
        EvaluationResponse evaluationResponse = factCheckingEvaluator.evaluate(evaluationRequest);
        if (!evaluationResponse.isPass()) {
            throw new InvalidAnswerException(message, answer);
        }
    }


    @Recover
    public ResponseEntity<String> recover(InvalidAnswerException exception) {
        return ResponseEntity.ok("به دلیل کافی نبودن منابع قادر به پاسخگویی درباره سوال شما نیستم.");
    }
}








