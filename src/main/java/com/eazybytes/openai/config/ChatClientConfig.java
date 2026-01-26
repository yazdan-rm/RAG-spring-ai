package com.eazybytes.openai.config;

import com.eazybytes.openai.advisors.TokenUsageAuditAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean(name = "fastChatClient")
    public ChatClient fastChatClient(ChatClient.Builder chatClientBuilder) {
        ChatOptions chatOptions = ChatOptions.builder().model("gemma3:1b")
                .temperature(0.8).build();

        return chatClientBuilder
                .defaultOptions(chatOptions)
                .defaultAdvisors(new TokenUsageAuditAdvisor())
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultSystem("""
                        You are an internal IT helpdesk assistant. Your role is to assist\s
                        employees with IT-related issues such as resetting passwords,\s
                        unlocking accounts, and answering questions related to IT policies.
                        If a user requests help with anything outside of these\s
                        responsibilities, respond politely and inform them that you are\s
                        only able to assist with IT support tasks within your defined scope
                        and do not answer more that three line then if user want more information you will answer more
                        than three line.
                        """)
                .build();
    }

    @Bean(name = "smartChatClient")
    public ChatClient smartChatClient(ChatClient.Builder chatClientBuilder) {
        ChatOptions chatOptions = ChatOptions.builder().model("gemma3:4b")
                .temperature(0.8).build();

        return chatClientBuilder
                .defaultAdvisors(new TokenUsageAuditAdvisor())
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultOptions(chatOptions)
                .build();
    }
}
