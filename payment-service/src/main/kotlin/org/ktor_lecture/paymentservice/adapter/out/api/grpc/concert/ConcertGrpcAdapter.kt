package org.ktor_lecture.paymentservice.adapter.out.api.grpc.concert

import kotlinx.coroutines.runBlocking
import org.ktor_lecture.paymentservice.adapter.out.api.response.ConcertReservationResponse
import org.ktor_lecture.paymentservice.application.port.out.ConcertApiClient
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ConcertGrpcAdapter (
    private val concertGrpcClient: ConcertGrpcClient,
): ConcertApiClient {
    override fun getReservation(reservationId: Long): ConcertReservationResponse {
        return ConcertReservationResponse(
            reservationId = 1L,
            userId = 1L,
            seatId = 1L,
            seatNo = 1,
            status = "test",
            price = 100,
            expiresAt = LocalDateTime.now().plusMinutes(10L)
        )
    }

    override fun reservationExpiredAndSeatAvaliable(reservationId: Long) {

    }

    override fun changeReservationPaid (reservationId: String) {
        runBlocking {
            concertGrpcClient.changeReservationPaid(reservationId)
        }
    }

    override fun changeSeatReserved(requestId: String) {
        runBlocking {
            concertGrpcClient.changeSeatReserved(requestId)
        }
    }

    override fun changeReservationPending(requestId: String) {
    }

    override fun changeSeatTemporarilyAssigned(requestId: String) {
    }
}