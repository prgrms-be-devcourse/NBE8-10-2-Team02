package com.back.global.igdb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient igdbRestClient(RestClient.Builder builder, IgdbProperties props) {
        return builder
                .baseUrl(props.baseUrl())
                .build();
    }

    @Bean
    public RestClient twitchAuthRestClient(RestClient.Builder builder) {
        // tokenUrl은 full url로 요청할 거라 baseUrl 없음
        return builder.build();
    }
}
