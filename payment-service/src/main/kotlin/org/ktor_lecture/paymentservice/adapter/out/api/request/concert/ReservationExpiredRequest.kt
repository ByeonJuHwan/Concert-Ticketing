package org.ktor_lecture.paymentservice.adapter.out.api.request.concert

data class ReservationExpiredRequest(
    val reservationId: Long,
)