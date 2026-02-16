package com.back.global.config;

import com.back.global.igdb.IgdbProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IgdbProperties.class)
public class IgdbConfig {
}
