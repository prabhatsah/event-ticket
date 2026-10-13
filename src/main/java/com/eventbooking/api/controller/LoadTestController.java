package com.eventbooking.api.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("loadtest")
@RequestMapping("/api/v1/load-test")
public class LoadTestController {

    @GetMapping("/cpu")
    public String generateCpuLoad() {

        double result = 0;

        for (int i = 0; i < 2_000_000; i++) {
            result += Math.sqrt(i);
        }

        return "CPU test completed: " + result;
    }
}