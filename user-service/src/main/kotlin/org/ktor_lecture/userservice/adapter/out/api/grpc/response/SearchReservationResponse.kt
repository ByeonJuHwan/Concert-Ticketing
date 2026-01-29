package org.ktor_lecture.userservice.adapter.out.api.grpc.response

data class SearchReservationResponse(
    val reservationId: Long,
    val reservationStatus: String,
    val seatId: Long,
    val price:Long,
)
