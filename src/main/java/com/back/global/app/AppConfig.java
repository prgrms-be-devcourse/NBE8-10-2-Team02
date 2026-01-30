package com.back.global.app;

import com.back.standard.util.Ut;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.ObjectMapper;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private static Environment environment;

    private final ObjectMapper objectMapper;

    @Autowired
    public void setEnvironment(Environment environment) {
        AppConfig.environment = environment;
    }

    public static boolean isDev() { return environment != null && environment.matchesProfiles("dev"); }
    public static boolean isTest() { return environment != null && environment.matchesProfiles("test"); }
    public static boolean isProd() { return environment != null && environment.matchesProfiles("prod"); }
    public static boolean isNotProd() { return !isProd(); }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @PostConstruct
    public void init() {
        Ut.json.objectMapper = objectMapper;
    }
}
