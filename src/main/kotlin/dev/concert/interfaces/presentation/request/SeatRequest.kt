package dev.concert.interfaces.presentation.request

data class SeatRequest(
    val concertOptionId : Long,
    val seatNo : Int,
)
