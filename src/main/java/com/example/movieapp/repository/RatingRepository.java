package com.example.movieapp.repository;

import com.example.movieapp.model.Movie;
import com.example.movieapp.model.Rating;
import com.example.movieapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByMovie(Movie movie);

    List<Rating> findByUser(User user);

    // Safer — queries by ID, avoids object reference mismatch
    List<Rating> findByUserId(Long userId);
}