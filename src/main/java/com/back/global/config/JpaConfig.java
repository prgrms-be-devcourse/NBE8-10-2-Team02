package com.back.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing //WebMvcTest를 위해 따로 JpaConfig를 만듦
public class JpaConfig {
}
