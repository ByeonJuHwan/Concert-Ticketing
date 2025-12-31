package org.ktor_lecture.paymentservice.adapter.out.api

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.ktor_lecture.paymentservice.adapter.out.api.request.concert.ChangeReservationPaidRequest
import org.ktor_lecture.paymentservice.adapter.out.api.request.concert.ChangeSeatReservedRequest
import org.ktor_lecture.paymentservice.adapter.out.api.request.concert.ChangeSeatTemporarilyAssignedRequest
import org.ktor_lecture.paymentservice.adapter.out.api.request.concert.ReservationExpiredRequest
import org.ktor_lecture.paymentservice.adapter.out.api.response.ConcertReservationResponse
import org.ktor_lecture.paymentservice.application.port.out.http.ConcertApiClient
import org.ktor_lecture.paymentservice.domain.exception.ConcertException
import org.ktor_lecture.paymentservice.domain.exception.ErrorCode
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestClient

@Primary
open class ConcertApiClientImpl(
    private val restClient: RestClient,
): ConcertApiClient {


    override fun getReservation(reservationId: Long): ConcertReservationResponse {
        return restClient.get()
            .uri("/reservations/$reservationId")
            .retrieve()
            .body(ConcertReservationResponse::class.java)
            ?: throw ConcertException(ErrorCode.RESERVATION_NOT_FOUND)
    }

    override fun reservationExpiredAndSeatAvaliable(reservationId: Long) {
        val request = ReservationExpiredRequest(reservationId)

        restClient.post()
            .uri("/reservations/expired")
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

    @CircuitBreaker(name = "concertService")
    override fun changeReservationPaid(requestId: String) {
        val request = ChangeReservationPaidRequest(requestId)

        restClient.post()
            .uri("/reservations/paid")
            .body(request)
            .retrieve()
            .toBodilessEntity()

    }

    @CircuitBreaker(name = "concertService")
    override fun changeSeatReserved(requestId: String) {
        val request = ChangeSeatReservedRequest(requestId)

        restClient.post()
            .uri("/reservations/seat/reserved")
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

    override fun changeReservationPending(requestId: String) {
        val request = ChangeReservationPaidRequest(requestId)

        restClient.post()
            .uri("/reservations/pending")
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

    override fun changeSeatTemporarilyAssigned(requestId: String) {
        val request = ChangeSeatTemporarilyAssignedRequest(requestId)

        restClient.post()
            .uri("/reservations/seat/temporarily-assign")
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }
}