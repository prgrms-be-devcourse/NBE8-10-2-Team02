package com.back.global.igdb;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "igdb")
public record IgdbProperties(
        String baseUrl,
        String clientId,
        String clientSecret,
        String tokenUrl
) {}
