package org.ktor_lecture.paymentservice.config

import org.ktor_lecture.paymentservice.adapter.out.api.ConcertApiClientImpl
import org.ktor_lecture.paymentservice.adapter.out.api.PointApiClientImpl
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class ApiClientConfig {

    @Value("\${api.client.user-service.url}")
    private lateinit var USER_SERVICE: String

    @Value("\${api.client.concert-service.url}")
    private lateinit var CONCERT_SERVICE: String




    @Bean
    fun pointApiClient(): PointApiClientImpl {
        return PointApiClientImpl(
            RestClient.builder()
                .baseUrl(USER_SERVICE)
                .build()
        )
    }

    @Bean
    fun concertApiClient(): ConcertApiClientImpl {
        return ConcertApiClientImpl(
            RestClient.builder()
                .baseUrl(CONCERT_SERVICE)
                .build()
        )
    }
}