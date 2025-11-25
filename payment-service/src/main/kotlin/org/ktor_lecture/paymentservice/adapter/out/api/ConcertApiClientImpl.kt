package org.ktor_lecture.paymentservice.adapter.out.api

import org.ktor_lecture.paymentservice.adapter.out.api.request.concert.ChangeReservationPaidRequest
import org.ktor_lecture.paymentservice.adapter.out.api.request.concert.ChangeSeatReservedRequest
import org.ktor_lecture.paymentservice.adapter.out.api.request.concert.ReservationExpiredRequest
import org.ktor_lecture.paymentservice.adapter.out.api.response.ConcertReservationResponse
import org.ktor_lecture.paymentservice.application.port.out.ConcertApiClient
import org.ktor_lecture.paymentservice.domain.exception.ConcertException
import org.ktor_lecture.paymentservice.domain.exception.ErrorCode
import org.springframework.web.client.RestClient

class ConcertApiClientImpl(
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

    override fun changeReservationPaid(requestId: String) {
        val request = ChangeReservationPaidRequest(requestId)

        restClient.post()
            .uri("/reservations/paid")
            .body(request)
            .retrieve()
            .toBodilessEntity()

    }

    override fun changeSeatReserved(requestId: String) {
        val request = ChangeSeatReservedRequest(requestId)

        restClient.post()
            .uri("/reservations/seat/reserved")
            .body(request)
            .retrieve()
    }
}