package com.example.movieapp.service;

import com.example.movieapp.model.Movie;
import java.util.List;

public interface RecommendationEngine {

    List<Movie> recommend(Movie seed, List<Movie> allMovies, int limit);

    String engineName();
}