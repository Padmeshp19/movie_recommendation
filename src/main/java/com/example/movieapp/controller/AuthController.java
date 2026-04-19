package com.example.movieapp.controller;

import com.example.movieapp.model.User;
import com.example.movieapp.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // -----------------------------------------------------------------------
    // Login
    // -----------------------------------------------------------------------

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("error", null);
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam String email,
                              @RequestParam String password,
                              HttpSession session,
                              Model model) {

        User user = userService.login(email, password);

        if (user == null) {
            // Wrong email or password — send error back to login page
            model.addAttribute("error", "Invalid email or password. Please try again.");
            return "login";
        }

        // Save logged-in user in session
        session.setAttribute("loggedInUser", user);
        return "redirect:/";
    }

    // -----------------------------------------------------------------------
    // Register
    // -----------------------------------------------------------------------

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("error", null);
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute User user,
                                 HttpSession session,
                                 Model model) {

        // Check if email already exists
        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("error", "An account with this email already exists.");
            model.addAttribute("user", user);
            return "register";
        }

        // Save the new user
        User saved = userService.registerUser(user);

        // Auto-login after registration
        session.setAttribute("loggedInUser", saved);
        return "redirect:/";
    }

    // -----------------------------------------------------------------------
    // Logout
    // -----------------------------------------------------------------------

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
