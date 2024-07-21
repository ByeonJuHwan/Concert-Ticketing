package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.QSeatEntity.seatEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.status.SeatStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport

interface ConcertSeatJpaRepository : JpaRepository<SeatEntity, Long>, ConcertSeatJpaRepositoryCustom

interface ConcertSeatJpaRepositoryCustom {
    fun findAvailableSeats(concertOptionId: Long): List<SeatEntity>
}

class ConcertSeatJpaRepositoryImpl : ConcertSeatJpaRepositoryCustom, QuerydslRepositorySupport(SeatEntity::class.java) {
    override fun findAvailableSeats(concertOptionId: Long): List<SeatEntity> {
        return from(seatEntity)
            .where(
                seatEntity.concertOption.id.eq(concertOptionId),
                seatEntity.seatStatus.eq(SeatStatus.AVAILABLE)
            )
            .fetch()
    }
}