package com.example.movieapp.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.movieapp.model.Movie;
import com.example.movieapp.repository.MovieRepository;
import com.example.movieapp.service.MovieService;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    @Override
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    @Override
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id).orElse(null);
    }

    @Override
    public Movie updateMovie(Long id, Movie movie) {

        Movie existingMovie = movieRepository.findById(id).orElse(null);

        if (existingMovie != null) {
            existingMovie.setTitle(movie.getTitle());
            existingMovie.setGenre(movie.getGenre());
            existingMovie.setLanguage(movie.getLanguage());
            existingMovie.setRating(movie.getRating());

            return movieRepository.save(existingMovie);
        }

        return null;
    }

    @Override
    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }
}