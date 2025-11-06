package org.ktor_lecture.concertservice.adapter.`in`.web.response

import org.ktor_lecture.concertservice.application.service.dto.ConcertSeatInfo
import org.ktor_lecture.concertservice.domain.status.SeatStatus

data class ConcertAvailableSeatsResponse (
    val concertOptionId : Long,
    val seats : List<ConcertSeatInfoResponse>
)

data class ConcertSeatInfoResponse(
    val seatId: Long,
    val seatNo: Int,
    val price: Long,
    val status: SeatStatus
) {
    companion object {
        fun from(info: ConcertSeatInfo): ConcertSeatInfoResponse {
            return ConcertSeatInfoResponse(
                seatId = info.seatId,
                seatNo = info.seatNo,
                price = info.price,
                status = info.status
            )
        }

        fun fromList(seats: List<ConcertSeatInfo>): List<ConcertSeatInfoResponse> {
            return seats.map { from(it) }
        }
    }
}