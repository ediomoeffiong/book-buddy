package com.bookbuddy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            boolean dbOk = (result != null && result == 1);
            return ResponseEntity.ok(Map.of(
                    "status", dbOk ? "UP" : "DOWN",
                    "database", dbOk ? "UP" : "DOWN"
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(503).body(Map.of(
                    "status", "DOWN",
                    "database", "DOWN",
                    "error", ex.getMessage()
            ));
        }
    }
}
