package dev.concert.presentation.response.concert

data class PaymentResponse(
    val reservationId : Long,
    val seatNo : Int,
    val concertVenue : String,
    val concertDate : String,
    val concertTime : String,
)
