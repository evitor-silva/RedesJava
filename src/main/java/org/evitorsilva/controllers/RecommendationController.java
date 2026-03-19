package org.evitorsilva.controllers;

import org.evitorsilva.entities.UserPreference;
import org.evitorsilva.services.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {
    
    @Autowired
    private RecommendationService recommendationService;
    

    @PostMapping("/preferences")
    public UserPreference savePreference(@RequestBody PreferenceRequest request) {
        return recommendationService.saveUserPreference(request.userId, request.itemId, request.rating);
    }
    
    @GetMapping("/user/{userId}")
    public List<RecommendationService.RecommendationDTO> getRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int k,
            @RequestParam(defaultValue = "10") int count) {
        return recommendationService.getRecommendationsForUser(userId, k, count);
    }
    
    @GetMapping("/neighbors/{userId}")
    public List<RecommendationService.UserSimilarityDTO> getSimilarUsers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int k) {
        return recommendationService.getSimilarUsers(userId, k);
    }
    
    public static class PreferenceRequest {
        public Long userId;
        public Long itemId;
        public Double rating;
    }
}
