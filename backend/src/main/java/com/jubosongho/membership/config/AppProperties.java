package com.jubosongho.membership.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Cors cors, Bootstrap bootstrap) {

    public record Jwt(String secret, int expirationHours) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Bootstrap(String superAdminUsername, String superAdminPassword,
                            boolean seedSampleZila, String sampleZilaAdminPassword) {
    }
}
