package dev.concert.config

import dev.concert.interfaces.presentation.filter.LoggingFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FilterConfig{

    @Bean
    fun loggingFilterRegistration(): FilterRegistrationBean<LoggingFilter> {
        val registrationBean = FilterRegistrationBean<LoggingFilter>()
        registrationBean.filter = LoggingFilter()
        registrationBean.addUrlPatterns("/*")
        return registrationBean
    }
}