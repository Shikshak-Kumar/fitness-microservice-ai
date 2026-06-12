package com.fitness.aiservice.service;

import com.fitness.aiservice.dto.GroqMessage;
import com.fitness.aiservice.dto.GroqRequest;
import com.fitness.aiservice.dto.GroqResponse;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class GroqService {

    private final WebClient webClient;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    public GroqService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String getRecommendation(String details) {

        try {

            GroqRequest request =
                    new GroqRequest(
                            "llama-3.3-70b-versatile",
                            List.of(
                                    new GroqMessage(
                                            "user",
                                            details
                                    )
                            )
                    );

            GroqResponse response =
                    webClient.post()
                            .uri(apiUrl)
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    "Bearer " + apiKey
                            )
                            .bodyValue(request)
                            .retrieve()
                            .bodyToMono(GroqResponse.class)
                            .block();

            if (response == null) {
                return "No response received from AI service.";
            }

            if (response.getChoices() == null ||
                    response.getChoices().isEmpty()) {

                return "AI service returned no recommendations.";
            }

            GroqResponse.Choice choice =
                    response.getChoices().getFirst();

            if (choice == null) {
                return "Invalid AI response.";
            }

            if (choice.getMessage() == null) {
                return "Invalid AI response message.";
            }

            String content =
                    choice.getMessage().getContent();

            if (content == null ||
                    content.isBlank()) {

                return "Empty recommendation generated.";
            }

            return content;
        } catch (Exception e) {

            e.printStackTrace();

            return "AI service temporarily unavailable.";

        }
    }
}