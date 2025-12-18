package org.ktor_lecture.concertservice.application.service.dto

import org.ktor_lecture.concertservice.domain.status.ReservationStatus
import java.time.LocalDateTime

data class ReserveSeatInfo (
    val reservationId: Long,
    val status : ReservationStatus,
    val reservationExpireTime : LocalDateTime,
)