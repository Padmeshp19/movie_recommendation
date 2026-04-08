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
import com.example.movieapp.service.MovieService;
import com.example.movieapp.service.impl.HybridRecommendationService;

import java.util.List;

@Controller
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;

    @Autowired
    private HybridRecommendationService hybridService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("")
    public String listMovies(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "movies";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("movie", new Movie());
        return "add-movie";
    }

    @PostMapping("")
    public String saveMovie(@ModelAttribute Movie movie) {
        movieService.saveMovie(movie);
        return "redirect:/movies";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("movie", movieService.getMovieById(id));
        return "edit-movie";
    }

    @PostMapping("/update/{id}")
    public String updateMovie(@PathVariable Long id,
                              @ModelAttribute Movie movie) {
        movieService.updateMovie(id, movie);
        return "redirect:/movies";
    }

    @GetMapping("/delete/{id}")
    public String deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return "redirect:/movies";
    }

    // -----------------------------------------------------------------------
    // Recommendations endpoint
    // -----------------------------------------------------------------------

    @GetMapping("/{id}/recommendations")
    public String recommendations(@PathVariable Long id, Model model) {
        Movie seed = movieService.getMovieById(id);
        List<Movie> recs = hybridService.getRecommendations(seed, 5);
        model.addAttribute("seed", seed);
        model.addAttribute("recommendations", recs);
        return "recommendations";
    }
}
