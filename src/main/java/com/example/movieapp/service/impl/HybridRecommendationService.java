package com.example.movieapp.service.impl;

import com.example.movieapp.model.Movie;
import com.example.movieapp.model.Rating;
import com.example.movieapp.service.MovieService;
import com.example.movieapp.service.RatingService;
import com.example.movieapp.service.RecommendationEngine;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hybrid Recommendation Service.
 *
 * STRATEGY:
 *   final_score(movie) = α * contentScore(movie)
 *                      + (1 - α) * collaborativeScore(movie)
 *
 *   α = CONTENT_WEIGHT (default 0.6)
 *   Tweak α to shift emphasis: closer to 1.0 → more content-driven,
 *   closer to 0.0 → more popularity/rating-driven.
 *
 * COLLABORATIVE SIGNAL:
 *   We use item-level average rating as a lightweight collaborative proxy.
 *   Why it works: high average ratings reflect aggregated user approval —
 *   a simple but effective signal when you don't yet have per-user
 *   rating history or a full user-item matrix.
 *   Normalised to [0, 1] by dividing by MAX_SCORE (10.0).
 *
 * SOLID — Single Responsibility:
 *   This class only combines signals. Content logic lives in ContentBasedEngine.
 *
 * SOLID — Dependency Inversion:
 *   Depends on RecommendationEngine interface, not ContentBasedEngine directly.
 *   You can inject a different engine (e.g. TF-IDF, Word2Vec) with zero changes here.
 *
 * SOLID — Open/Closed:
 *   Add more engines by injecting a List<RecommendationEngine> and weighting them —
 *   this class doesn't need to change.
 *
 * HOW TO USE IN YOUR CONTROLLER:
 *
 *   @GetMapping("/movies/{id}/recommendations")
 *   public String recommendations(@PathVariable Long id, Model model) {
 *       Movie seed = movieService.getMovieById(id);
 *       List<Movie> recs = hybridService.getRecommendations(seed, 5);
 *       model.addAttribute("recommendations", recs);
 *       model.addAttribute("seed", seed);
 *       return "recommendations";
 *   }
 */
@Service
public class HybridRecommendationService {

    // -----------------------------------------------------------------------
    // Tuneable constants
    // -----------------------------------------------------------------------

    /** Weight given to the content-based score (0.0 – 1.0). */
    private static final double CONTENT_WEIGHT = 0.6;

    /** Maximum possible rating score in your system (normalisation denominator). */
    private static final double MAX_SCORE = 10.0;

    // -----------------------------------------------------------------------
    // Dependencies — injected via constructor (best practice / testable)
    // -----------------------------------------------------------------------

    private final RecommendationEngine contentEngine;
    private final MovieService movieService;
    private final RatingService ratingService;

    /**
     * Spring injects ContentBasedEngine for RecommendationEngine because it is
     * the only bean implementing that interface. If you add more engines later,
     * use @Qualifier to specify which one to inject here.
     */
    public HybridRecommendationService(RecommendationEngine contentEngine,
                                       MovieService movieService,
                                       RatingService ratingService) {
        this.contentEngine  = contentEngine;
        this.movieService   = movieService;
        this.ratingService  = ratingService;
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Returns top {@code limit} hybrid recommendations for a seed movie.
     *
     * @param seed  the movie to base recommendations on
     * @param limit how many results to return
     * @return ordered list, best match first
     */
    public List<Movie> getRecommendations(Movie seed, int limit) {

        List<Movie> allMovies = movieService.getAllMovies();

        // --- Step 1: Content-based candidates ---
        // Ask the engine for top-(limit * 3) so we have enough to re-rank
        // after mixing in the collaborative score.
        int candidatePool = limit * 3;
        List<Movie> contentCandidates = contentEngine.recommend(seed, allMovies, candidatePool);

        // --- Step 2: Collaborative scores for candidates ---
        Map<Long, Double> collabScores = buildCollaborativeScores(contentCandidates);

        // --- Step 3: Content scores (positional rank → normalised score) ---
        Map<Long, Double> contentScores = buildPositionalScores(contentCandidates);

        // --- Step 4: Blend and re-rank ---
        return contentCandidates.stream()
                .map(movie -> {
                    double content  = contentScores.getOrDefault(movie.getId(), 0.0);
                    double collab   = collabScores.getOrDefault(movie.getId(), 0.0);
                    double hybrid   = CONTENT_WEIGHT * content
                                    + (1.0 - CONTENT_WEIGHT) * collab;
                    return Map.entry(movie, hybrid);
                })
                .sorted(Map.Entry.<Movie, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Collaborative score = average rating for each movie, normalised to [0, 1].
     *
     * Movies with no ratings get a neutral score of 0.5 — they're neither
     * penalised (0.0) nor rewarded (1.0), keeping them in the mix.
     */
    private Map<Long, Double> buildCollaborativeScores(List<Movie> movies) {
        Map<Long, Double> scores = new HashMap<>();

        for (Movie movie : movies) {
            List<Rating> ratings = ratingService.getRatingsByMovie(movie.getId());

            double normalised;
            if (ratings.isEmpty()) {
                normalised = 0.5; // neutral — no data yet
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

    /**
     * Converts a ranked list from the content engine into normalised scores.
     *
     * Position 0 (best match) → 1.0
     * Position N-1 (worst match) → approaches 0.0
     *
     * Formula: score = 1 - (rank / total)
     * This preserves the ordering signal while mapping to [0, 1].
     */
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
