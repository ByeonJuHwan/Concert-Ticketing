package dev.concert.application.concert.dto

import dev.concert.domain.entity.status.ReservationStatus
import java.time.LocalDateTime

data class ConcertReservationResponseDto(
    val status : ReservationStatus,
    val reservationExpireTime : LocalDateTime,
)
