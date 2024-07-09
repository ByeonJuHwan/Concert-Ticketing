package dev.concert.infrastructure

import dev.concert.domain.SeatRepository
import dev.concert.domain.entity.SeatEntity
import dev.concert.infrastructure.jpa.SeatJpaRepository
import org.springframework.stereotype.Repository

@Repository
class SeatRepositoryImpl (
    private val seatJpaRepository: SeatJpaRepository,
) : SeatRepository{
    override fun getSeatWithLock(seatId: Long): SeatEntity? {
        return seatJpaRepository.getSeatWithLock(seatId)
    }
}