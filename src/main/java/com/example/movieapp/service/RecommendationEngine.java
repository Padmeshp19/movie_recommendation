package com.example.movieapp.service;

import com.example.movieapp.model.Movie;
import java.util.List;

/**
 * Contract for any recommendation strategy.
 *
 * SOLID — Dependency Inversion Principle:
 *   High-level components (HybridRecommendationService, controllers)
 *   depend on this abstraction, NOT on concrete engines.
 *   Swap or add engines without touching callers.
 *
 * SOLID — Open/Closed Principle:
 *   Add a new strategy (e.g. DeepLearningEngine) by implementing
 *   this interface — no existing code needs to change.
 */
public interface RecommendationEngine {

    /**
     * Given a seed movie, return up to {@code limit} recommended movies
     * from the full catalogue, ordered by descending relevance score.
     *
     * @param seed      the movie the user is currently viewing / liked
     * @param allMovies the full catalogue to score against
     * @param limit     maximum number of results to return
     * @return ordered list of recommended movies (seed excluded)
     */
    List<Movie> recommend(Movie seed, List<Movie> allMovies, int limit);

    /**
     * Human-readable name for this engine — used in logging & UI labels.
     */
    String engineName();
}
