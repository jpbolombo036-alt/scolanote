package com.bulletin.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class RailwayEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.trim().isEmpty()) {
            return;
        }

        try {
            URI uri = new URI(databaseUrl.trim());
            String username = uri.getUserInfo() != null ? uri.getUserInfo().split(":")[0] : "";
            String password = uri.getUserInfo() != null ? uri.getUserInfo().split(":")[1] : "";
            String host = uri.getHost();
            int port = uri.getPort();
            String database = uri.getPath().startsWith("/") ? uri.getPath().substring(1) : uri.getPath();

            if (host == null || database.isEmpty()) {
                return;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("spring.datasource.url", "jdbc:postgresql://" + host + ":" + port + "/" + database);
            map.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
            map.put("spring.datasource.username", username);
            map.put("spring.datasource.password", password);
            environment.getPropertySources().addFirst(new MapPropertySource("railway-database-config", map));
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Format DATABASE_URL Railway invalide : " + databaseUrl + ". Format attendu : postgresql://user:pass@host:port/db");
        }
    }
}
