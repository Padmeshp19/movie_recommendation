package com.example.movieapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String loginSuccess() {
        return "redirect:/";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
}