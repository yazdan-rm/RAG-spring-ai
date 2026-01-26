package com.eazybytes.openai.controller;

import com.eazybytes.openai.model.CountryCities;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StructuredOutPutController {

    private final ChatClient chatClient;

    public StructuredOutPutController(@Qualifier("fastChatClient") ChatClient fastChatClient) {
        this.chatClient = fastChatClient;
    }

    @GetMapping("/chat-bean")
    public ResponseEntity<CountryCities> chatBean(@RequestParam("message") String message) {
        CountryCities entity = chatClient
                .prompt()
                .user(message)
                .call().entity(CountryCities.class);
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @GetMapping("/chat-list")
    public ResponseEntity<List<String>> chatList(@RequestParam("message") String message) {
        List<String> entity = chatClient
                .prompt()
                .user(message)
                .call().entity(new ListOutputConverter());
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @GetMapping("/chat-map")
    public ResponseEntity<Map<String, Object>> chatMap(@RequestParam("message") String message) {
        Map<String, Object> entity = chatClient
                .prompt()
                .user(message)
                .call().entity(new MapOutputConverter());
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @GetMapping("/chat-bean-list")
    public ResponseEntity<List<CountryCities>> chatBeanList(@RequestParam("message") String message) {
        List<CountryCities> entity = chatClient
                .prompt()
                .user(message)
                .call().entity(new ParameterizedTypeReference<List<CountryCities>>() {
                });
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

}











