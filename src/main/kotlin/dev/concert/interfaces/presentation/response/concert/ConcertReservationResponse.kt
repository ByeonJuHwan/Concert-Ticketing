package dev.concert.interfaces.presentation.response.concert

import dev.concert.application.concert.dto.ConcertReservationResponseDto
import dev.concert.domain.entity.status.ReservationStatus
import java.time.LocalDateTime

data class ConcertReservationResponse(
    val status : ReservationStatus,
    val reservationExpireTime : LocalDateTime,
    val message : String = "임시 예약이 완료되었습니다.",
)

fun ConcertReservationResponseDto.toResponse() = ConcertReservationResponse(
    status = status,
    reservationExpireTime = reservationExpireTime,
)