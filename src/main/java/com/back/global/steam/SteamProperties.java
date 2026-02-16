package com.back.global.steam;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "steam")
public record SteamProperties(
        String baseUrl,
        String apiKey
) {
}
