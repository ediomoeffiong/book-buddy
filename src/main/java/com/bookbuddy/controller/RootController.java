package com.bookbuddy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RootController {
    
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Book Buddy API");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("documentation", "/api/auth/register - POST - Register a new user");
        return ResponseEntity.ok(response);
    }
}

