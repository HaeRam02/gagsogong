package com.example.gagso.WorkRoom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api")

public class TaskController {
    @GetMapping("/tasks")
    public String home() {
        return "gkdl";
    }
}
