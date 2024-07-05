package dev.concert.presentation.response.concert

import dev.concert.domain.entity.status.ReservationStatus
import java.time.LocalDateTime

data class SeatResponse(
    val status : ReservationStatus,
    val expiresAt : LocalDateTime,
)
