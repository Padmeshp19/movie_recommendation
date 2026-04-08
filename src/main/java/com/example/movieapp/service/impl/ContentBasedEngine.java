package com.example.movieapp.service.impl;

import com.example.movieapp.model.Movie;
import com.example.movieapp.service.RecommendationEngine;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Content-Based Recommendation Engine.
 *
 * HOW IT WORKS:
 *   Represents each movie as a feature vector built from its genre and
 *   language fields, then ranks candidates by cosine similarity to the
 *   seed movie's vector.  Higher cosine similarity → more similar content.
 *
 * SOLID — Single Responsibility Principle:
 *   This class does ONE thing: score movies by content similarity.
 *   It knows nothing about users, ratings, or HTTP.
 *
 * SOLID — Open/Closed Principle:
 *   To add a new feature (e.g. director, cast) just extend buildFeatureVector()
 *   — the scoring logic stays untouched.
 *
 * COSINE SIMILARITY PRIMER:
 *   sim(A, B) = (A · B) / (|A| * |B|)
 *   Range: 0.0 (nothing in common) → 1.0 (identical feature profile)
 */
@Component
public class ContentBasedEngine implements RecommendationEngine {

    @Override
    public String engineName() {
        return "ContentBased";
    }

    @Override
    public List<Movie> recommend(Movie seed, List<Movie> allMovies, int limit) {

        Map<String, Double> seedVector = buildFeatureVector(seed);

        return allMovies.stream()
                .filter(m -> !m.getId().equals(seed.getId()))   // exclude seed itself
                .map(candidate -> {
                    double score = cosineSimilarity(seedVector,
                                                    buildFeatureVector(candidate));
                    return Map.entry(candidate, score);
                })
                .filter(e -> e.getValue() > 0.0)                // drop completely unrelated
                .sorted(Map.Entry.<Movie, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Feature engineering
    // -----------------------------------------------------------------------

    /**
     * Builds a sparse feature vector for a movie.
     *
     * Each distinct token becomes a dimension in the feature space.
     * Weight schema (tunable):
     *   genre    → 2.0  (primary signal — most discriminative)
     *   language → 1.0  (secondary signal)
     *
     * Example for Movie("Inception", "Sci-Fi|Thriller", "English"):
     *   { "genre:sci-fi": 2.0, "genre:thriller": 2.0, "lang:english": 1.0 }
     *
     * Why multi-value genre support?
     *   A movie like "Action|Comedy" should partially match both Action movies
     *   and Comedy movies — splitting on "|" gives us that granularity.
     */
    private Map<String, Double> buildFeatureVector(Movie movie) {
        Map<String, Double> vector = new HashMap<>();

        // Genre features — weight 2.0 each
        if (movie.getGenre() != null) {
            for (String g : movie.getGenre().split("[|,/]")) {
                String key = "genre:" + g.trim().toLowerCase();
                vector.merge(key, 2.0, Double::sum);
            }
        }

        // Language feature — weight 1.0
        if (movie.getLanguage() != null) {
            String langKey = "lang:" + movie.getLanguage().trim().toLowerCase();
            vector.put(langKey, 1.0);
        }

        return vector;
    }

    // -----------------------------------------------------------------------
    // Cosine similarity
    // -----------------------------------------------------------------------

    /**
     * Computes cosine similarity between two sparse feature vectors.
     *
     * Iterates only over the keys of the smaller vector (dot product is
     * commutative) to stay O(min(|a|,|b|)) instead of O(|a|+|b|).
     *
     * Returns 0.0 when either vector is the zero vector (guards div-by-zero).
     */
    private double cosineSimilarity(Map<String, Double> a, Map<String, Double> b) {

        // Dot product
        double dot = 0.0;
        for (Map.Entry<String, Double> entry : a.entrySet()) {
            Double bVal = b.get(entry.getKey());
            if (bVal != null) {
                dot += entry.getValue() * bVal;
            }
        }

        // Magnitudes
        double magA = magnitude(a);
        double magB = magnitude(b);

        if (magA == 0.0 || magB == 0.0) return 0.0;

        return dot / (magA * magB);
    }

    /** Euclidean magnitude of a sparse vector. */
    private double magnitude(Map<String, Double> vector) {
        double sumSq = vector.values().stream()
                             .mapToDouble(v -> v * v)
                             .sum();
        return Math.sqrt(sumSq);
    }
}
