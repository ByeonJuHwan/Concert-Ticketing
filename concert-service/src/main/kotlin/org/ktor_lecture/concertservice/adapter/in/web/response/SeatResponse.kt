package org.ktor_lecture.concertservice.adapter.`in`.web.response

import org.ktor_lecture.concertservice.domain.status.ReservationStatus
import java.time.LocalDateTime

data class SeatResponse(
    val status : ReservationStatus,
    val expiresAt : LocalDateTime,
)
