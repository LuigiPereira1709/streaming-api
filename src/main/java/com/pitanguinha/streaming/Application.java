package com.pitanguinha.streaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Streaming Service API", version = "1.0", description = "API for managing and consuming streaming media such as music and podcasts."))
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
