package org.ktor_lecture.userservice.adapter.out.api.http

import org.ktor_lecture.userservice.adapter.out.api.grpc.response.SearchConcertsResponse
import org.ktor_lecture.userservice.adapter.out.api.grpc.response.SearchReservationResponse
import org.ktor_lecture.userservice.application.port.out.UserConcertReservationHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class UserConcertReservationApiClientImpl(
    @Value("\${api.client.concert-service.url}")
    private val CONCERT_SERVICE: String
): UserConcertReservationHttpClient {

    val concertRestClient = RestClient.builder()
        .baseUrl(CONCERT_SERVICE)
        .build()

    override fun searchConcerts(userId: Long): List<SearchConcertsResponse> {
        return concertRestClient.get()
            .uri("/concerts")
            .retrieve()
            .body<List<SearchConcertsResponse>>()
            ?: throw RuntimeException("콘서트 조회에 실패했습니다")
    }

    override fun searchReservations(userId: Long): List<SearchReservationResponse> {
        return concertRestClient.get()
            .uri("/concerts/reservations/{userId}", userId)
            .retrieve()
            .body<List<SearchReservationResponse>>()
            ?: throw RuntimeException("예약정보 조회에 실패했습니다")
    }
}