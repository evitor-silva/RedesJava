package org.evitorsilva.services;

import org.evitorsilva.entities.User;
import org.evitorsilva.entities.UserPreference;
import java.util.*;
import java.util.stream.Collectors;

public class KNearestNeighborsAlgorithm {
    
    public static double calculateCosineSimilarity(List<UserPreference> prefs1, List<UserPreference> prefs2) {
        if (prefs1.isEmpty() || prefs2.isEmpty()) {
            return 0.0;
        }
        
        Map<Long, Double> ratingMap1 = prefs1.stream()
            .collect(Collectors.toMap(p -> p.getItem().getId(), UserPreference::getRating));
        
        Map<Long, Double> ratingMap2 = prefs2.stream()
            .collect(Collectors.toMap(p -> p.getItem().getId(), UserPreference::getRating));
        
        Set<Long> commonItems = new HashSet<>(ratingMap1.keySet());
        commonItems.retainAll(ratingMap2.keySet());
        
        if (commonItems.isEmpty()) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        for (Long itemId : commonItems) {
            dotProduct += ratingMap1.get(itemId) * ratingMap2.get(itemId);
        }
        
        double magnitude1 = Math.sqrt(prefs1.stream()
            .mapToDouble(p -> p.getRating() * p.getRating())
            .sum());
        
        double magnitude2 = Math.sqrt(prefs2.stream()
            .mapToDouble(p -> p.getRating() * p.getRating())
            .sum());
        
        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0.0;
        }
        
        return dotProduct / (magnitude1 * magnitude2);
    }
    
    public static List<UserSimilarity> findKNearestNeighbors(User targetUser,
                                                             List<User> allUsers, 
                                                             Map<Long, List<UserPreference>> userPreferencesMap,
                                                             int k) {
        List<UserPreference> targetUserPrefs = userPreferencesMap.getOrDefault(targetUser.getId(), new ArrayList<>());

        return allUsers.stream()
            .filter(user -> !user.getId().equals(targetUser.getId()))
            .map(user -> {
                List<UserPreference> userPrefs = userPreferencesMap.getOrDefault(user.getId(), new ArrayList<>());
                double similarity = calculateCosineSimilarity(targetUserPrefs, userPrefs);
                return new UserSimilarity(user, similarity);
            })
            .filter(us -> us.similarity > 0)
            .sorted((a, b) -> Double.compare(b.similarity, a.similarity))
            .limit(k)
            .collect(Collectors.toList());
    }
    
    public static List<RecommendedItem> getRecommendations(User targetUser,
                                                           List<UserSimilarity> neighbors,
                                                           Map<Long, List<UserPreference>> userPreferencesMap,
                                                           List<Long> targetUserItemIds,
                                                           int recommendationCount) {
        Set<Long> userRatedItems = new HashSet<>(targetUserItemIds);
        
        Map<Long, Double> itemScores = new HashMap<>();
        Map<Long, Integer> itemCounts = new HashMap<>();
        
        for (UserSimilarity neighbor : neighbors) {
            List<UserPreference> neighborPrefs = userPreferencesMap.getOrDefault(neighbor.user.getId(), new ArrayList<>());
            
            for (UserPreference pref : neighborPrefs) {
                Long itemId = pref.getItem().getId();
                
                if (userRatedItems.contains(itemId)) {
                    continue;
                }
                
                double weightedRating = pref.getRating() * neighbor.similarity;
                itemScores.put(itemId, itemScores.getOrDefault(itemId, 0.0) + weightedRating);
                itemCounts.put(itemId, itemCounts.getOrDefault(itemId, 0) + 1);
            }
        }
        
        return itemScores.entrySet().stream()
            .map(entry -> {
                long itemId = entry.getKey();
                double score = entry.getValue() / itemCounts.get(itemId);
                return new RecommendedItem(itemId, score);
            })
            .sorted((a, b) -> Double.compare(b.score, a.score))
            .limit(recommendationCount)
            .collect(Collectors.toList());
        
    }
    
    public static class UserSimilarity {
        public User user;
        public double similarity;
        
        public UserSimilarity(User user, double similarity) {
            this.user = user;
            this.similarity = similarity;
        }
    }
    
    public static class RecommendedItem {
        public long itemId;
        public double score;
        
        public RecommendedItem(long itemId, double score) {
            this.itemId = itemId;
            this.score = score;
        }
    }
}
