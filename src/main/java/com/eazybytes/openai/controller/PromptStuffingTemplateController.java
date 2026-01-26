package com.eazybytes.openai.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PromptStuffingTemplateController {

    private final ChatClient fastChatClient;


    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    Resource systemPromptTemplate;

    public PromptStuffingTemplateController(@Qualifier("fastChatClient") ChatClient fastChatClient) {
        this.fastChatClient = fastChatClient;
    }


    @GetMapping("/prompt-stuffing")
    public String promptStuffing(@RequestParam("message") String message) {
        return fastChatClient
                .prompt()
                .system(systemPromptTemplate)
                .user(message)
                .call().content();
    }
}











