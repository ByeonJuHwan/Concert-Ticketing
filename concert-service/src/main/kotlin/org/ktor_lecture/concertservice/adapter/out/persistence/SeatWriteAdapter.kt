package org.ktor_lecture.concertservice.adapter.out.persistence

import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.SeatJpaRepository
import org.ktor_lecture.concertservice.application.port.out.SeatRepository
import org.ktor_lecture.concertservice.domain.entity.SeatEntity
import org.ktor_lecture.concertservice.domain.exception.ConcertException
import org.ktor_lecture.concertservice.domain.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SeatWriteAdapter(
    private val seatJpaRepository: SeatJpaRepository,
): SeatRepository {


    override fun getSeatWithLock(seatId: Long): SeatEntity? {
        return seatJpaRepository.getSeatWithLock(seatId)
    }

    override fun updateSeatStatusToAvailable(seatIds: List<Long>) {
        seatJpaRepository.updateSeatStatusToAvailable(seatIds)
    }

    @Transactional
    override fun save(seat: SeatEntity): SeatEntity {
        return seatJpaRepository.save(seat)
    }

    @Transactional
    override fun deleteAll() {
        return seatJpaRepository.deleteAll()
    }
}