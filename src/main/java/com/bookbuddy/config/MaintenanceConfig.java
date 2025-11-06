package com.bookbuddy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.maintenance")
@Data
public class MaintenanceConfig {
    
    private boolean enabled;
    private String message;
}

