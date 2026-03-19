package org.evitorsilva.services;

import org.evitorsilva.entities.Item;
import org.evitorsilva.entities.User;
import org.evitorsilva.entities.UserPreference;
import org.evitorsilva.repositories.ItemRepository;
import org.evitorsilva.repositories.UserRepository;
import org.evitorsilva.repositories.UserPreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private UserPreferenceRepository userPreferenceRepository;
    
    private static final int DEFAULT_K = 5;
    private static final int DEFAULT_RECOMMENDATION_COUNT = 10;
    

    public UserPreference saveUserPreference(Long userId, Long itemId, Double rating) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
        
        UserPreference preference = new UserPreference(user, item, rating);
        return userPreferenceRepository.save(preference);
    }

    public List<RecommendationDTO> getRecommendationsForUser(Long userId, int k, int recommendationCount) {
        User targetUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        List<User> allUsers = userRepository.findAll();
        
        Map<Long, List<UserPreference>> userPreferencesMap = new HashMap<>();
        for (User user : allUsers) {
            List<UserPreference> prefs = userPreferenceRepository.findByUserId(user.getId());
            userPreferencesMap.put(user.getId(), prefs);
        }
        
        List<UserPreference> targetUserPrefs = userPreferenceRepository.findByUserId(userId);
        List<Long> targetUserItemIds = targetUserPrefs.stream()
            .map(p -> p.getItem().getId())
            .collect(Collectors.toList());
        
        List<KNearestNeighborsAlgorithm.UserSimilarity> neighbors =
            KNearestNeighborsAlgorithm.findKNearestNeighbors(targetUser, allUsers, userPreferencesMap, k);
        
        List<KNearestNeighborsAlgorithm.RecommendedItem> recommendedItems = 
            KNearestNeighborsAlgorithm.getRecommendations(targetUser, neighbors, userPreferencesMap, 
                                                           targetUserItemIds, recommendationCount);
        
        return recommendedItems.stream()
            .map(rec -> {
                Item item = itemRepository.findById(rec.itemId)
                    .orElseThrow(() -> new IllegalArgumentException("Item not found"));
                return new RecommendationDTO(item.getId(), item.getName(), item.getCategory(), rec.score);
            })
            .collect(Collectors.toList());
    }
    

    public List<RecommendationDTO> getRecommendationsForUser(Long userId) {
        return getRecommendationsForUser(userId, DEFAULT_K, DEFAULT_RECOMMENDATION_COUNT);
    }
    

    public List<UserSimilarityDTO> getSimilarUsers(Long userId, int k) {
        User targetUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        List<User> allUsers = userRepository.findAll();
        
        Map<Long, List<UserPreference>> userPreferencesMap = new HashMap<>();
        for (User user : allUsers) {
            List<UserPreference> prefs = userPreferenceRepository.findByUserId(user.getId());
            userPreferencesMap.put(user.getId(), prefs);
        }
        
        List<KNearestNeighborsAlgorithm.UserSimilarity> neighbors = 
            KNearestNeighborsAlgorithm.findKNearestNeighbors(targetUser, allUsers, userPreferencesMap, k);
        
        return neighbors.stream()
            .map(us -> new UserSimilarityDTO(us.user.getId(), us.user.getName(), us.similarity))
            .collect(Collectors.toList());
    }
    
    public static class RecommendationDTO {
        public Long itemId;
        public String itemName;
        public String category;
        public Double score;
        
        public RecommendationDTO(Long itemId, String itemName, String category, Double score) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.category = category;
            this.score = score;
        }
    }
    
    public static class UserSimilarityDTO {
        public Long userId;
        public String userName;
        public Double similarity;
        
        public UserSimilarityDTO(Long userId, String userName, Double similarity) {
            this.userId = userId;
            this.userName = userName;
            this.similarity = similarity;
        }
    }
}
