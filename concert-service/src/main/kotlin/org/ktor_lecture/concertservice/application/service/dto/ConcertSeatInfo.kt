package org.ktor_lecture.concertservice.application.service.dto

import org.ktor_lecture.concertservice.domain.status.SeatStatus

data class ConcertSeatInfo (
    val seatId: Long,
    val seatNo: Int,
    val price: Long,
    val status: SeatStatus
)