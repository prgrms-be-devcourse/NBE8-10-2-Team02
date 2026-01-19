package com.back.global.igdb;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IgdbProperties.class)
public class IgdbConfig {

}
