package dev.concert.config

import dev.concert.interfaces.presentation.interceptor.ActiveTokenValidateInterceptor
import dev.concert.interfaces.presentation.interceptor.TokenValidateInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class InterceptorConfig (
    private val tokenValidateInterceptor: TokenValidateInterceptor,
    private val activeTokenValidateInterceptor: ActiveTokenValidateInterceptor,
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(activeTokenValidateInterceptor)
            .addPathPatterns("/concerts/**", "/payment/**")
    }
}