package org.ktor_lecture.concertservice.application.port.out

import org.ktor_lecture.concertservice.domain.entity.SeatEntity

interface SeatRepository {

    fun getSeatWithLock(seatId: Long): SeatEntity?
    fun updateSeatStatusToAvailable(seatIds: List<Long>)
    fun save(seat: SeatEntity): SeatEntity
    fun deleteAll()
}