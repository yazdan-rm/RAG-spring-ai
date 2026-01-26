package com.eazybytes.openai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatClient fastChatClient;
    private final ChatClient smartChatClient;

    public ChatController(
            @Qualifier("fastChatClient") ChatClient fastChatClient,
            @Qualifier("smartChatClient") ChatClient smartChatClient
    ) {
        this.fastChatClient = fastChatClient;
        this.smartChatClient = smartChatClient;
    }

    @GetMapping("/fast-chat")
    public String fastChat(@RequestParam("message") String message) {
        return fastChatClient
                .prompt()
                .user(message)
                .call().content();
    }

    @GetMapping("/smart-chat")
    public String smartChat(@RequestParam("message") String message) {
        return smartChatClient.prompt(message).call().content();
    }
}
