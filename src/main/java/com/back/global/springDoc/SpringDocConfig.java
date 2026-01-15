package com.back.global.springDoc;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @SecurityScheme
 * OpenAPI 문서에서 실제 요청 테스트를 할 때
 * Http 요청 헤더의 Authorization 헤더에 들어가는 값을
 * OpenAPI 문서에서 직접 설정할 수 있도록 하는 어노테이션
 * Authorization header 방식의 로그인 버튼을 우측 상단에 만들어준다.
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "API 서버", version = "beta", description = "API 서버 문서입니다."))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SpringDocConfig {
    @Bean
    public GroupedOpenApi groupApiV1() {
        return GroupedOpenApi.builder()
                .group("apiV1")
                .pathsToMatch("/api/v1/**")
                .build();
    }

    @Bean
    public GroupedOpenApi groupController() {
        return GroupedOpenApi.builder()
                .group("home")
                .pathsToExclude("/api/**")
                .build();
    }
}
