package dev.concert.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class SwaggerConfig {

    @Bean
    fun openApi() : OpenAPI {
        val info  = Info().title("Concert API").description("콘서트 예약 대기열 API").version("1.0.0")

        val securitySchemeName = "Authorization"

        val securityRequirement = SecurityRequirement()
            .addList(securitySchemeName)

        val components = Components()
            .addSecuritySchemes(
                securitySchemeName, SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            )

        return OpenAPI().info(info)
            .addSecurityItem(securityRequirement)
            .components(components)
    }
}