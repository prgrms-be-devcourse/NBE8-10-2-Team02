package com.back.global.config;

import com.back.global.igdb.IgdbProperties;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IgdbProperties.class)
public class IgdbConfig {

    @Bean
    public RateLimiter igdbRateLimiter() {
        return RateLimiter.create(4.0);
    }
}
