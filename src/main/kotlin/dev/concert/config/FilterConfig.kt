package dev.concert.config

import dev.concert.presentation.filter.LoggingFilter
import dev.concert.presentation.filter.TokenValidateFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FilterConfig (
    private val tokenValidateFilter: TokenValidateFilter,
) {

    @Bean
    fun loggingFilterRegistration(): FilterRegistrationBean<LoggingFilter> {
        val registrationBean = FilterRegistrationBean<LoggingFilter>()
        registrationBean.filter = LoggingFilter()
        registrationBean.addUrlPatterns("/*")
        registrationBean.order = 1
        return registrationBean
    }

    @Bean
    fun tokenFilterRegistration(): FilterRegistrationBean<TokenValidateFilter> {
        val registrationBean = FilterRegistrationBean<TokenValidateFilter>()
        registrationBean.filter = tokenValidateFilter
        registrationBean.addUrlPatterns("/concerts/*", "/payment/*")
        registrationBean.order = 2
        return registrationBean
    }
}