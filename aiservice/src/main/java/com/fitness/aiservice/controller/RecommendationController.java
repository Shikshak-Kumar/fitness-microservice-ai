package com.fitness.aiservice.controller;

import com.fitness.aiservice.models.Recommendation;
import com.fitness.aiservice.service.RecommendationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Recommendation>> getUserRecommendations(@PathVariable String userId) {
        return ResponseEntity.ok(recommendationService.getUserRecommendations(userId));
    }

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<Recommendation> getActivityRecommendations(@PathVariable String activityId) {
        return ResponseEntity.ok(recommendationService.getActivityRecommendations(activityId));
    }




}
