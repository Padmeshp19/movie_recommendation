package com.example.movieapp.controller;

import com.example.movieapp.model.Movie;
import com.example.movieapp.model.Rating;
import com.example.movieapp.model.User;
import com.example.movieapp.service.MovieService;
import com.example.movieapp.service.RatingService;

import jakarta.servlet.http.HttpSession;

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
    public String showRatingForm(@PathVariable Long movieId,
                                 HttpSession session,
                                 Model model) {
        // Redirect to login if not logged in
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("movieId", movieId);
        return "add-rating";
    }

    @PostMapping("/save")
    public String saveRating(@RequestParam int score,
                             @RequestParam String review,
                             @RequestParam Long movieId,
                             HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        Movie movie = movieService.getMovieById(movieId);

        Rating rating = new Rating(score, review, movie);
        rating.setUser(user);

        ratingService.saveRating(rating);

        return "redirect:/movies";
    }
}