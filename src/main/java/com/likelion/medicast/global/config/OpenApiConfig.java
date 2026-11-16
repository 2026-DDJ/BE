package com.likelion.medicast.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MEDICAST API",
                version = "v1",
                description = "건강·의료이용 정보를 바탕으로 예상 연간 의료비를 제공하는 API입니다. "
                        + "예측 결과는 통계적 추정이며 진단, 치료 권고, 보험 견적 또는 실제 비용 보장이 아닙니다.",
                license = @License(name = "Private project")
        )
)
@SecurityScheme(
        name = OpenApiConfig.BEARER_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "로그인 또는 회원가입 응답의 accessToken을 입력합니다."
)
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";
}
