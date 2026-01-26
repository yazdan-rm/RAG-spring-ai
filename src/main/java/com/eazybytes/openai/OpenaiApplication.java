package com.eazybytes.openai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class OpenaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenaiApplication.class, args);
    }

}
