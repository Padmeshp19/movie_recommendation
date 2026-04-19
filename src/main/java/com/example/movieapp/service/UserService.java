package com.example.movieapp.service;

import com.example.movieapp.model.User;
import com.example.movieapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /** Save a new user to the database. */
    public User registerUser(User user) {
        return userRepository.save(user);
    }

    /** Returns user if email + password match, null otherwise. */
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /** Check if an email is already registered — used during signup. */
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email) != null;
    }
}
