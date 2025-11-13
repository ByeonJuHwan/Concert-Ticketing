package org.ktor_lecture.concertservice.adapter.out.persistence

import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.SeatJpaRepository
import org.ktor_lecture.concertservice.application.port.out.SeatRepository
import org.ktor_lecture.concertservice.domain.entity.SeatEntity
import org.springframework.stereotype.Component

@Component
class SeatWriteAdapter(
    private val seatJpaRepository: SeatJpaRepository,
): SeatRepository {


    override fun getSeatWithLock(seatId: Long): SeatEntity? {
        return seatJpaRepository.getSeatWithLock(seatId)
    }
}