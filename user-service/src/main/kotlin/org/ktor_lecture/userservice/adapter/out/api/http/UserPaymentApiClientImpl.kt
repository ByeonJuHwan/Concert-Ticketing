package org.ktor_lecture.userservice.adapter.out.api.http

import org.ktor_lecture.userservice.adapter.out.api.grpc.response.SearchUserPaymentResponse
import org.ktor_lecture.userservice.adapter.out.api.grpc.response.SearchUserReservationResponse
import org.ktor_lecture.userservice.application.port.out.UserPaymentHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class UserPaymentApiClientImpl(
    @Value("\${api.client.payment-service.url}")
    private val PAYMENT_SERVICE: String,

    @Value("\${api.client.concert-service.url}")
    private val CONCERT_SERVICE: String
): UserPaymentHttpClient {



    val reservationRestClient = RestClient.builder()
        .baseUrl(CONCERT_SERVICE)
        .build()

    val paymentRestClient = RestClient.builder()
        .baseUrl(PAYMENT_SERVICE)
        .build()

    override fun searchUserPayments(userId: Long): List<SearchUserPaymentResponse> {
        return paymentRestClient.get()
            .uri("/payments/users/{userId}", userId)
            .retrieve()
            .body<List<SearchUserPaymentResponse>>()
            ?: throw RuntimeException("결제 정보 조회에 실패했습니다")
    }

    override fun searchUserReservations(userId: Long): List<SearchUserReservationResponse> {
        return reservationRestClient.get()
            .uri("/concerts/reservations/{userId}", userId)
            .retrieve()
            .body<List<SearchUserReservationResponse>>()
            ?: throw RuntimeException("예약정보 조회에 실패했습니다")
    }
}