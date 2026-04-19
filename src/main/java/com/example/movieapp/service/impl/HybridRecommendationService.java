package com.example.movieapp.service.impl;

import com.example.movieapp.model.Movie;
import com.example.movieapp.model.Rating;
import com.example.movieapp.service.MovieService;
import com.example.movieapp.service.RatingService;
import com.example.movieapp.service.RecommendationEngine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HybridRecommendationService {

    private static final double CONTENT_WEIGHT = 0.6;
    private static final double MAX_SCORE = 10.0;

    private final RecommendationEngine contentEngine;
    private final MovieService movieService;
    private final RatingService ratingService;

    public HybridRecommendationService(
            @Qualifier("contentBasedEngine") RecommendationEngine contentEngine,
            MovieService movieService,
            RatingService ratingService) {
        this.contentEngine = contentEngine;
        this.movieService  = movieService;
        this.ratingService = ratingService;
    }

    public List<Movie> getRecommendations(Movie seed, int limit) {

        List<Movie> allMovies = movieService.getAllMovies();

        int candidatePool = limit * 3;
        List<Movie> contentCandidates = contentEngine.recommend(seed, allMovies, candidatePool);

        Map<Long, Double> collabScores  = buildCollaborativeScores(contentCandidates);
        Map<Long, Double> contentScores = buildPositionalScores(contentCandidates);

        return contentCandidates.stream()
                .map(movie -> {
                    double content = contentScores.getOrDefault(movie.getId(), 0.0);
                    double collab  = collabScores.getOrDefault(movie.getId(), 0.0);
                    double hybrid  = CONTENT_WEIGHT * content
                                   + (1.0 - CONTENT_WEIGHT) * collab;
                    return Map.entry(movie, hybrid);
                })
                .sorted(Map.Entry.<Movie, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Map<Long, Double> buildCollaborativeScores(List<Movie> movies) {
        Map<Long, Double> scores = new HashMap<>();
        for (Movie movie : movies) {
            List<Rating> ratings = ratingService.getRatingsByMovie(movie.getId());
            double normalised;
            if (ratings.isEmpty()) {
                normalised = 0.5;
            } else {
                double avg = ratings.stream()
                                    .mapToInt(Rating::getScore)
                                    .average()
                                    .orElse(0.0);
                normalised = avg / MAX_SCORE;
            }
            scores.put(movie.getId(), normalised);
        }
        return scores;
    }

    private Map<Long, Double> buildPositionalScores(List<Movie> rankedMovies) {
        Map<Long, Double> scores = new HashMap<>();
        int total = rankedMovies.size();
        for (int i = 0; i < total; i++) {
            double score = (total == 1) ? 1.0 : 1.0 - ((double) i / total);
            scores.put(rankedMovies.get(i).getId(), score);
        }
        return scores;
    }
}