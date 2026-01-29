package org.ktor_lecture.concertservice.adapter.`in`.web.response

import org.ktor_lecture.concertservice.application.service.dto.ReserveSeatInfo
import org.ktor_lecture.concertservice.domain.status.ReservationStatus
import java.time.LocalDateTime

data class ConcertReservationStatusResponse(
    val status : ReservationStatus,
    val reservationExpireTime : LocalDateTime,
    val message : String = "임시 예약이 완료되었습니다.",
) {
    companion object {
        fun from(info: ReserveSeatInfo): ConcertReservationStatusResponse {
            return ConcertReservationStatusResponse(
                status = info.status,
                reservationExpireTime = info.reservationExpireTime,
            )
        }
    }
}

data class ConcertReservationResponse(
    val reservationId: Long,
    val userId : Long,
    val seatId : Long,
    val seatNo : Int,
    val status: String,
    val price: Long,
    val expiresAt: LocalDateTime,
)

data class ConcertUserReservationsResponse(
    val reservationId: Long,
    val reservationStatus: String,
)