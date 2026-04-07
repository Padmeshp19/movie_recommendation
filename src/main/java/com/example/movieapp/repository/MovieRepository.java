package com.example.movieapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.movieapp.model.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}