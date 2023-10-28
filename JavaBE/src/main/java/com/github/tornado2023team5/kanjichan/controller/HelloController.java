package com.github.tornado2023team5.kanjichan.controller;

import com.github.tornado2023team5.kanjichan.model.HelloForm;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/public/hello")
    public String hello() {
        return "Hello, world!";
    }

    @PostMapping("/auth/hello")
    public String hello2(@RequestBody HelloForm form) {
        return form.toString();
    }
}
