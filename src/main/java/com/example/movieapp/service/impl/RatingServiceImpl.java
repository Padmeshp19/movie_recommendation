package com.example.movieapp.service.impl;

import com.example.movieapp.model.Movie;
import com.example.movieapp.model.Rating;
import com.example.movieapp.repository.MovieRepository;
import com.example.movieapp.repository.RatingRepository;
import com.example.movieapp.service.RatingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final MovieRepository movieRepository;

    public RatingServiceImpl(RatingRepository ratingRepository,
                             MovieRepository movieRepository) {
        this.ratingRepository = ratingRepository;
        this.movieRepository = movieRepository;
    }

    @Override
    public Rating saveRating(Rating rating) {
        return ratingRepository.save(rating);
    }

    @Override
    public List<Rating> getRatingsByMovie(Long movieId) {

        Movie movie = movieRepository.findById(movieId).orElse(null);

        return ratingRepository.findByMovie(movie);
    }
}