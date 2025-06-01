package com.example.gagso.WorkRoom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class TaskController {
    @GetMapping("/")
    public String home() {
        return "Spring Boot is running!";
    }
}
