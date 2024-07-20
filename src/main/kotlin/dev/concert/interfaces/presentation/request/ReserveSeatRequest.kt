package dev.concert.interfaces.presentation.request

import dev.concert.application.concert.dto.ConcertReservationDto

data class ReserveSeatRequest(
    val seatId : Long,
    val userId  : Long,
)

fun ReserveSeatRequest.toDto() =  ConcertReservationDto(
    seatId = seatId,
    userId = userId,
)
