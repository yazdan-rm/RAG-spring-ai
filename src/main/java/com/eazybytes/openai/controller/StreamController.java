package com.eazybytes.openai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
public class StreamController {

    private final ChatClient fastChatClient;

    public StreamController(@Qualifier("fastChatClient") ChatClient fastChatClient) {
        this.fastChatClient = fastChatClient;
    }

    @GetMapping("/stream")
    public Flux<String> fastChat(@RequestParam("message") String message) {
        return fastChatClient.prompt().user(message).stream().content();
    }

}
