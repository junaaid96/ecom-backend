package com.ecom.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "application.cors")
public class CorsProperties {

    @NotEmpty
    private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:3000"));

    @NotEmpty
    private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

    @NotEmpty
    private List<String> allowedHeaders = new ArrayList<>(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));

    private List<String> exposedHeaders = new ArrayList<>(List.of("Authorization"));

    private boolean allowCredentials = true;

    @PositiveOrZero
    private long maxAge = 3600;
}

