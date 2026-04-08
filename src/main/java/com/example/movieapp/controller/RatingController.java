package com.example.movieapp.controller;

import com.example.movieapp.model.Movie;
import com.example.movieapp.model.Rating;
import com.example.movieapp.service.MovieService;
import com.example.movieapp.service.RatingService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ratings")
public class RatingController {

    private final RatingService ratingService;
    private final MovieService movieService;

    public RatingController(RatingService ratingService,
                             MovieService movieService) {
        this.ratingService = ratingService;
        this.movieService = movieService;
    }

    @GetMapping("/add/{movieId}")
    public String showRatingForm(@PathVariable Long movieId, Model model) {

        model.addAttribute("movieId", movieId);
        return "add-rating";
    }

    @PostMapping("/save")
    public String saveRating(@RequestParam int score,
                            @RequestParam String review,
                            @RequestParam Long movieId) {

        Movie movie = movieService.getMovieById(movieId);

        Rating rating = new Rating(score, review, movie);

        ratingService.saveRating(rating);

        return "redirect:/movies";
    }
}