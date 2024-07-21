package dev.concert.config

import dev.concert.interfaces.presentation.interceptor.TokenValidateInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class InterceptorConfig (
    private val tokenValidateInterceptor: TokenValidateInterceptor,
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(tokenValidateInterceptor)
            .addPathPatterns("/concerts/**", "/payment/**")
    }
}