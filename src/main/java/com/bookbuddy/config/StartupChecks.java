package com.bookbuddy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
public class StartupChecks implements ApplicationRunner {

    private final Environment env;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    public StartupChecks(Environment env) {
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // If running with production profile, ensure a persistent datasource is configured
        if (Arrays.asList(env.getActiveProfiles()).contains("prod")) {
            if (datasourceUrl == null || datasourceUrl.isBlank() || datasourceUrl.contains("jdbc:h2:mem")) {
                log.error("Production profile requires a persistent datasource. spring.datasource.url is not set or points to in-memory H2: {}", datasourceUrl);
                throw new IllegalStateException("Persistent datasource not configured for production. Set DATABASE_URL, DATABASE_USERNAME and DATABASE_PASSWORD.");
            }
            log.info("Production datasource validated: {}", datasourceUrl);
        }
    }
}
