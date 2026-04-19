package com.example.movieapp.service.impl;

import com.example.movieapp.model.Movie;
import com.example.movieapp.model.Rating;
import com.example.movieapp.model.User;
import com.example.movieapp.repository.RatingRepository;
import com.example.movieapp.service.RecommendationEngine;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class UserBasedRecommendationEngine implements RecommendationEngine {

    private final RatingRepository ratingRepository;

    public UserBasedRecommendationEngine(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @Override
    public List<Movie> recommend(Movie seed, List<Movie> allMovies, int limit) {
        return Collections.emptyList();
    }

    @Override
    public String engineName() {
        return "User-Based Engine";
    }

    public List<Movie> recommendForUser(User user, List<Movie> allMovies, int limit) {
        Long userId = user.getId();
        System.out.println("=== recommendForUser called, userId=" + userId);

        // Query by user ID directly
        List<Rating> ratings = ratingRepository.findByUserId(userId);
        System.out.println("=== ratings found: " + ratings.size());

        if (ratings.isEmpty()) return Collections.emptyList();

        // Sum scores per genre
        Map<String, Integer> genreScore = new HashMap<>();
        for (Rating r : ratings) {
            String genre = r.getMovie().getGenre();
            int score = r.getScore();
            System.out.println("=== rating: genre=" + genre + " score=" + score);
            if (genre != null) {
                genreScore.merge(genre, score, Integer::sum);
            }
        }

        System.out.println("=== genreScores: " + genreScore);

        // Favourite genre
        String favGenre = Collections.max(
                genreScore.entrySet(),
                Map.Entry.comparingByValue()
        ).getKey();

        System.out.println("=== favGenre: " + favGenre);

        // Already rated movie IDs
        Set<Long> ratedIds = ratings.stream()
                .map(r -> r.getMovie().getId())
                .collect(Collectors.toSet());

        System.out.println("=== ratedIds: " + ratedIds);

        List<Movie> result = allMovies.stream()
                .filter(m -> {
                    boolean genreMatch = m.getGenre() != null &&
                                        m.getGenre().trim().equalsIgnoreCase(favGenre.trim());
                    boolean notRated = !ratedIds.contains(m.getId());
                    System.out.println("=== movie: " + m.getTitle() +
                                       " genre=" + m.getGenre() +
                                       " genreMatch=" + genreMatch +
                                       " notRated=" + notRated);
                    return genreMatch && notRated;
                })
                .sorted((a, b) -> Double.compare(
                        b.getRating() == null ? 0 : b.getRating(),
                        a.getRating() == null ? 0 : a.getRating()))
                .limit(limit)
                .collect(Collectors.toList());

        System.out.println("=== final recs count: " + result.size());
        return result;
    }

    public String getFavouriteGenre(User user) {
        List<Rating> ratings = ratingRepository.findByUserId(user.getId());
        if (ratings.isEmpty()) return null;

        Map<String, Integer> genreScore = new HashMap<>();
        for (Rating r : ratings) {
            String genre = r.getMovie().getGenre();
            if (genre != null) {
                genreScore.merge(genre, r.getScore(), Integer::sum);
            }
        }

        return Collections.max(
                genreScore.entrySet(),
                Map.Entry.comparingByValue()
        ).getKey();
    }
}