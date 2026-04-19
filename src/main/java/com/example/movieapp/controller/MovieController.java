package com.example.movieapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.movieapp.model.Movie;
import com.example.movieapp.model.User;
import com.example.movieapp.service.MovieService;
import com.example.movieapp.service.impl.HybridRecommendationService;
import com.example.movieapp.service.impl.UserBasedRecommendationEngine;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
public class MovieController {

    private final MovieService movieService;

    @Autowired
    private HybridRecommendationService hybridService;

    @Autowired
    private UserBasedRecommendationEngine userBasedEngine;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    // -----------------------------------------------------------------------
    // Movies CRUD
    // -----------------------------------------------------------------------

    @GetMapping("/movies")
    public String listMovies(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "movies";
    }

    @GetMapping("/movies/new")
    public String showCreateForm(Model model) {
        model.addAttribute("movie", new Movie());
        return "add-movie";
    }

    @PostMapping("/movies")
    public String saveMovie(@ModelAttribute Movie movie) {
        movieService.saveMovie(movie);
        return "redirect:/movies";
    }

    @GetMapping("/movies/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("movie", movieService.getMovieById(id));
        return "edit-movie";
    }

    @PostMapping("/movies/update/{id}")
    public String updateMovie(@PathVariable Long id,
                              @ModelAttribute Movie movie) {
        movieService.updateMovie(id, movie);
        return "redirect:/movies";
    }

    @GetMapping("/movies/delete/{id}")
    public String deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return "redirect:/movies";
    }

    // -----------------------------------------------------------------------
    // User-based recommendations (logged-in user's rated genres)
    // -----------------------------------------------------------------------

        @GetMapping("/recommendations")
        public String userRecommendations(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "recommendations";
        }

        // DEBUG - print to console
        System.out.println("=== USER ID: " + user.getId());
        String favGenre = userBasedEngine.getFavouriteGenre(user);
        System.out.println("=== FAV GENRE: " + favGenre);
        List<Movie> allMovies = movieService.getAllMovies();
        List<Movie> recs = userBasedEngine.recommendForUser(user, allMovies, 10);
        System.out.println("=== RECS COUNT: " + recs.size());

        model.addAttribute("recommendations", recs);
        model.addAttribute("favGenre", favGenre);
        return "recommendations";
    }

    // -----------------------------------------------------------------------
    // Seed-based recommendations (click Recommend on a movie card)
    // -----------------------------------------------------------------------

    @GetMapping("/recommendations/{id}")
    public String seedRecommendations(@PathVariable Long id, Model model) {
        Movie seed = movieService.getMovieById(id);
        List<Movie> recs = hybridService.getRecommendations(seed, 5);
        model.addAttribute("seed", seed);
        model.addAttribute("recommendations", recs);
        return "recommendations";
    }

    // Keep old route working too
    @GetMapping("/movies/{id}/recommendations")
    public String legacyRecommendations(@PathVariable Long id, Model model) {
        return seedRecommendations(id, model);
    }
}