package dev.concert.infrastructure

import dev.concert.domain.repository.SeatRepository
import dev.concert.domain.entity.SeatEntity
import dev.concert.infrastructure.jpa.SeatJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class SeatRepositoryImpl (
    private val seatJpaRepository: SeatJpaRepository,
) : SeatRepository {
    override fun getSeatWithLock(seatId: Long): SeatEntity? {
        return seatJpaRepository.getSeatWithLock(seatId)
    }

    override fun save(seat: SeatEntity): SeatEntity {
        return seatJpaRepository.save(seat)
    }

    override fun findById(seatId: Long): SeatEntity? {
        return seatJpaRepository.findByIdOrNull(seatId)
    }

    override fun deleteAll() {
        seatJpaRepository.deleteAll()
    }
}