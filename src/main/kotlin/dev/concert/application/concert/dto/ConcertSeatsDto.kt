package dev.concert.application.concert.dto

import dev.concert.domain.entity.status.SeatStatus

data class ConcertSeatsDto(
    val seatId: Long,
    val seatNo: Int,
    val price: Long,
    val status: SeatStatus
)
