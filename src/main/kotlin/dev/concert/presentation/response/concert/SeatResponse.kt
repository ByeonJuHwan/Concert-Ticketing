package dev.concert.presentation.response.concert

import dev.concert.infrastructure.entity.status.ReservationStatus
import java.time.LocalDateTime

data class SeatResponse(
    val status : ReservationStatus,
    val expiresAt : LocalDateTime,
)
