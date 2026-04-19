package com.example.movieapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int score;
    private String review;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    // 🔥 NEW: link to user
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Rating() {}

    public Rating(int score, String review, Movie movie) {
        this.score = score;
        this.review = review;
        this.movie = movie;
    }

    public Long getId() { return id; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}