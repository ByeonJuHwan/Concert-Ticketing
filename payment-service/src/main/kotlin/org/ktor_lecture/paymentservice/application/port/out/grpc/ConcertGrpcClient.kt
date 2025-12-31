package org.ktor_lecture.paymentservice.application.port.out.grpc

import org.ktor_lecture.paymentservice.adapter.out.api.response.ConcertReservationResponse

interface ConcertGrpcClient {

    suspend fun getReservation(reservationId: Long): ConcertReservationResponse
    suspend fun reservationExpiredAndSeatAvaliable(reservationId: Long)
    suspend fun changeReservationPaid(reservationId: Long)
    suspend fun changeSeatReserved(reservationId: Long)
}