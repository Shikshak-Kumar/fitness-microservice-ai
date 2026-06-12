package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.models.Activity;
import com.fitness.aiservice.models.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {
    private final RecommendationRepository recommendationRepository;
    private final GroqService groqService;


    public Recommendation generateRecommendation(Activity activity) {

        String prompt = createPrompt(activity);

        String aiResponse = groqService.getRecommendation(prompt);

        log.info("RESPONSE FROM AI: {} ", aiResponse);

        Recommendation recommendation =
                Recommendation.builder()
                        .userId(activity.getUserId())
                        .activityId(activity.getId())
                        .recommendation(aiResponse)
                        .createdAt(LocalDateTime.now())
                        .build();

//        return recommendationRepository.save(recommendation);
        return processAIResponse(activity, aiResponse);
    }

    private Recommendation processAIResponse(Activity activity, String aiResponse) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            JsonNode rootNode = mapper.readTree(aiResponse);

            JsonNode analysisNode = rootNode.path("analysis");

            StringBuilder fullAnalysis = new StringBuilder();

            addAnalysisSection(
                    fullAnalysis,
                    analysisNode,
                    "overall",
                    "Overall: "
            );

            addAnalysisSection(
                    fullAnalysis,
                    analysisNode,
                    "pace",
                    "Pace: "
            );

            addAnalysisSection(
                    fullAnalysis,
                    analysisNode,
                    "heartRate",
                    "Heart Rate: "
            );

            addAnalysisSection(
                    fullAnalysis,
                    analysisNode,
                    "caloriesBurned",
                    "Calories: "
            );

            List<String> improvements = extractImprovements(rootNode.path("improvements"));

            List<String> suggestions = extractSuggestions(rootNode.path("suggestions"));

            List<String> safety = extractSafety(rootNode.path("safety"));

            Recommendation recommendation =
                    Recommendation.builder()
                            .activityId(activity.getId())
                            .userId(activity.getUserId())
                            .recommendation(
                                    fullAnalysis.toString()
                            )
                            .improvements(
                                    improvements
                            )
                            .suggestions(
                                    suggestions
                            )
                            .safety(
                                    safety
                            )
                            .createdAt(
                                    LocalDateTime.now()
                            )
                            .build();

            return recommendationRepository.save(
                    recommendation
            );

        } catch (Exception e) {

            log.error(
                    "Error parsing AI response",
                    e
            );

            return createDefaultRecommendation(
                    activity
            );
        }
    }

    private Recommendation createDefaultRecommendation(
            Activity activity) {

        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .recommendation("Unable to generate AI recommendation.")
                .improvements(List.of("Continue your current routine."))
                .suggestions(List.of("Try again later."))
                .safety(List.of("Stay hydrated.", "Warm up before exercise."))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<String> extractSafety(JsonNode safetyNode) {
        List<String> safetyList = new ArrayList<>();
        if(safetyNode.isArray()) {
            safetyNode.forEach(safety->{
                safetyList.add(safety.asText());
            });
        }
        return safetyList.isEmpty() ? Collections.singletonList("Follow general safety guidelines.") : safetyList;
    }

    private List<String> extractSuggestions(JsonNode suggestionNode) {
        List<String> suggestions = new ArrayList<>();
        if(suggestionNode.isArray()){
            suggestionNode.forEach(suggestion -> {
                String workout = suggestion.path("workout").asText();
                String description = suggestion.path("description").asText();
                suggestions.add(String.format("%s: %s", workout, description));
            });
        }

        return suggestions.isEmpty() ? Collections.singletonList("No specific suggestion provided!") : suggestions;
    }


    private List<String> extractImprovements(JsonNode improvementNode) {
        List<String> improvements = new ArrayList<>();

        if(improvementNode.isArray()){
            improvementNode.forEach(
                    improvement -> {
                        String area = improvement.path("area").asText();
                        String recommendation = improvement.path("recommendation").asText();
                        improvements.add(String.format("%s: %s", area,recommendation));
                    }
            );
        }

        return improvements.isEmpty() ? Collections.singletonList("No specific improvements provided!") : improvements;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix){
        if(!analysisNode.path(key).isMissingNode()){
            fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asText())
                    .append("\n");
        }
    }

    private String createPrompt(Activity activity) {

        return String.format("""
        Analyze this fitness activity and provide detailed recommendations.

        Return ONLY valid JSON in this EXACT format:

        {
          "analysis": {
            "overall": "Overall analysis here",
            "pace": "Pace analysis here",
            "heartRate": "Heart rate analysis here",
            "caloriesBurned": "Calories analysis here"
          },
          "improvements": [
            {
              "area": "Area name",
              "recommendation": "Detailed recommendation"
            }
          ],
          "suggestions": [
            {
              "workout": "Workout name",
              "description": "Detailed workout description"
            }
          ],
          "safety": [
            "Safety point 1",
            "Safety point 2"
          ]
        }

        Activity Details:

        Activity Type: %s
        Duration: %d minutes
        Calories Burned: %d
        Additional Metrics: %s

        Important Rules:
        1. Return ONLY JSON.
        2. Do not add markdown.
        3. Do not use ```json blocks.
        4. Ensure valid JSON.
        5. Fill all sections.
        6. Suggestions should be practical and actionable.
        7. Safety recommendations should be specific to this activity.

        """,
                activity.getActivityType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }
}
