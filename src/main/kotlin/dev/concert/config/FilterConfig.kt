package dev.concert.config

import dev.concert.presentation.filter.TokenValidateFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

//@Configuration
class FilterConfig (
    private val tokenValidateFilter: TokenValidateFilter,
) {

//    @Bean
//    fun tokenFilterRegistration(): FilterRegistrationBean<TokenValidateFilter> {
//        val registrationBean = FilterRegistrationBean<TokenValidateFilter>()
//        registrationBean.filter = tokenValidateFilter
//        registrationBean.addUrlPatterns("/concerts/*", "/payment/*")
//        return registrationBean
//    }
}