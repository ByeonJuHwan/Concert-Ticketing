package org.ktor_lecture.concertservice.application.port.out

import org.ktor_lecture.concertservice.domain.entity.SeatEntity

interface SeatRepository {

    fun getSeatWithLock(seatId: Long): SeatEntity?
}