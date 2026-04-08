package com.example.movieapp.service;

import java.util.List;

import com.example.movieapp.model.Movie;

public interface MovieService {

    Movie saveMovie(Movie movie);

    List<Movie> getAllMovies();

    Movie getMovieById(Long id);

    Movie updateMovie(Long id, Movie movie);

    void deleteMovie(Long id);
}