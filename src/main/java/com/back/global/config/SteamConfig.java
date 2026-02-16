package com.back.global.config;

import com.back.global.steam.SteamProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SteamProperties.class)
public class SteamConfig {
}
