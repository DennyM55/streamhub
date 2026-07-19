package com.dennymathew.streamhub.home;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/")
class HomeController {

    @GetMapping
    public String home() {
        return "Welcome to StreamHub!";
    }
}
