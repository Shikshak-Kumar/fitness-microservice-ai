package com.fitness.activityservice.service;

import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class UserValidationService {

    private final WebClient userWebClient;

    public Boolean validateUser(String userId) {
        try{
            return userWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

        } catch (WebClientResponseException e) {
            e.printStackTrace();
        }
        return false;
    }

}
