package dev.concert.presentation.request

data class SeatRequest(
    val concertOptionId : Long,
    val seatNo : Int,
)
