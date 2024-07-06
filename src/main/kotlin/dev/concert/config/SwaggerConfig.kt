package dev.concert.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openApi() : OpenAPI {
        val info  = Info().title("Concert API").description("콘서트 예약 대기열 API").version("1.0.0")
        return OpenAPI().info(info)
    }
}