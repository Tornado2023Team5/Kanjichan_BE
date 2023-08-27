package com.github.tornado2023team5.kanjichan.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @GetMapping("/ping")
    public String health() {
        return "OK";
    }

    @GetMapping("/callback/ping")
    public void health2() {
        System.out.println("OK");
    }
}
