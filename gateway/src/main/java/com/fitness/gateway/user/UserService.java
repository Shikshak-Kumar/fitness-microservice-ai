package com.fitness.gateway.user;

import com.fitness.gateway.dto.RegisterRequest;
import com.fitness.gateway.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final WebClient userWebClient;

    public Mono<Boolean> validateUser(String userId) {
        log.info("Calling User Service for {}", userId);
        try{
            return userWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .onErrorResume(WebClientResponseException.class, e->{
                        if(e.getStatusCode()== HttpStatus.NOT_FOUND){
                            return Mono.error(new RuntimeException("User not found: " + userId));
                        }

                        else if(e.getStatusCode()== HttpStatus.BAD_REQUEST){
                            return Mono.error(new RuntimeException("Invalid: " + userId));
                        }

                        return Mono.error( new RuntimeException("Unexpected error: "+ userId));
                    });

        } catch (WebClientResponseException e) {
            return Mono.error(
                    new RuntimeException("Failed to validate user", e)
            );
        }
    }

    public Mono<UserResponse> registerUser(RegisterRequest registerRequest) {
       log.info("Calling user registration for {}", registerRequest.getEmail());
        try{
            return userWebClient.post()
                    .uri("/api/users/register")
                    .bodyValue(registerRequest)
                    .retrieve()
                    .bodyToMono(UserResponse.class)
                    .onErrorResume(WebClientResponseException.class, e->{
                        if(e.getStatusCode()== HttpStatus.NOT_FOUND){
                            return Mono.error(new RuntimeException("Bad request: " + e.getMessage()));
                        }

                        return Mono.error( new RuntimeException("Unexpected error: "+ e.getMessage()));
                    });

        } catch (WebClientResponseException e) {
            return Mono.error(
                    new RuntimeException("Failed to validate user", e)
            );
        }
    }
}
