package org.ktor_lecture.paymentservice.adapter.out.api.response

import java.time.LocalDateTime

data class ConcertReservationResponse(
    val reservationId: Long,
    val userId : Long,
    val seatId : Long,
    val seatNo : Int,
    val status: String,
    val price: Long,
    val expiresAt: LocalDateTime,
)