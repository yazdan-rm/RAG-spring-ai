package com.eazybytes.openai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class ChatMemoryChatClientConfig {


    @Value("classpath:/promptTemplates/evaluationPromptTemplate.st")
    Resource evaluationPromptTemplate;

    @Bean(name = "chatMemoryChatClient")
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder
            , ChatMemory chatMemory, RetrievalAugmentationAdvisor retrievalAugmentationAdvisor) {
        ChatOptions chatOptions = ChatOptions.builder().model("gemma3:4b")
                .temperature(0.7).build();
        Advisor loggerAdvisor = new SimpleLoggerAdvisor();
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return chatClientBuilder
                .defaultAdvisors(List.of(loggerAdvisor, memoryAdvisor, retrievalAugmentationAdvisor))
                .defaultOptions(chatOptions)
                .build();
    }

    @Bean
    public ChatMemory ChatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .build();
    }

    @Bean
    public FactCheckingEvaluator factCheckingEvaluator(ChatClient.Builder chatClient) throws IOException {
        // Ensure the cloned client also uses the correct model
        ChatClient.Builder clonedChatClientBuilder = chatClient.clone()
                .defaultOptions(ChatOptions.builder()
                        .model("gemma3:1b")
                        .temperature(0.7)
                        .build());

        return FactCheckingEvaluator.builder(clonedChatClientBuilder).evaluationPrompt(
                evaluationPromptTemplate.getContentAsString(StandardCharsets.UTF_8)).build();
    }


    @Bean
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(ChatClient.Builder chatClient, VectorStore vectorStore) {

        // Ensure the cloned client also uses the correct model
        ChatClient.Builder clonedChatClientBuilder = chatClient.clone()
                .defaultOptions(ChatOptions.builder()
                        .model("gemma3:1b")
                        .temperature(0.7)
                        .build());

        return RetrievalAugmentationAdvisor.builder()
                .queryTransformers(TranslationQueryTransformer.builder()
                        .chatClientBuilder(clonedChatClientBuilder)
                        .targetLanguage("Persian")
                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .topK(3)
                        .similarityThreshold(0.5)
                        .build())
//                .documentPostProcessors()
                .build();
    }
}

