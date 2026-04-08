package com.example.movieapp.service;

import com.example.movieapp.model.Rating;
import java.util.List;

public interface RatingService {

    Rating saveRating(Rating rating);

    List<Rating> getRatingsByMovie(Long movieId);
}