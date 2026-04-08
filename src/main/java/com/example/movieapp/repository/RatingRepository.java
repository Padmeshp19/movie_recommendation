package com.example.movieapp.repository;

import com.example.movieapp.model.Rating;
import com.example.movieapp.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByMovie(Movie movie);
}