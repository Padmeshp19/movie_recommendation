package com.example.movieapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.movieapp.model.Movie;
import com.example.movieapp.model.User;
import com.example.movieapp.service.MovieService;
import com.example.movieapp.service.impl.HybridRecommendationService;
import com.example.movieapp.service.impl.UserBasedRecommendationEngine;

import jakarta.servlet.http.HttpSession;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.ArrayList;
import java.util.Collections;
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
    // Helper: scan static/posters/ and return list of filenames
    // -----------------------------------------------------------------------
    private List<String> getPosterFiles() {
        List<String> posters = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:static/posters/*");
            for (Resource r : resources) {
                String filename = r.getFilename();
                if (filename != null && (
                        filename.toLowerCase().endsWith(".jpg") ||
                        filename.toLowerCase().endsWith(".jpeg") ||
                        filename.toLowerCase().endsWith(".png") ||
                        filename.toLowerCase().endsWith(".webp"))) {
                    posters.add(filename);
                }
            }
            Collections.sort(posters);
        } catch (Exception e) {
            // No posters folder yet — return empty list
        }
        return posters;
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
        model.addAttribute("posters", getPosterFiles());
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
        model.addAttribute("posters", getPosterFiles());
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
    // User-based recommendations
    // -----------------------------------------------------------------------

    @GetMapping("/recommendations")
    public String userRecommendations(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "recommendations";
        }

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
    // Seed-based recommendations
    // -----------------------------------------------------------------------

    @GetMapping("/recommendations/{id}")
    public String seedRecommendations(@PathVariable Long id, Model model) {
        Movie seed = movieService.getMovieById(id);
        List<Movie> recs = hybridService.getRecommendations(seed, 5);
        model.addAttribute("seed", seed);
        model.addAttribute("recommendations", recs);
        return "recommendations";
    }

    @GetMapping("/movies/{id}/recommendations")
    public String legacyRecommendations(@PathVariable Long id, Model model) {
        return seedRecommendations(id, model);
    }
}