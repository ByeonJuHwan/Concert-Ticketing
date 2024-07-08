package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.QConcertEntity.concertEntity
import dev.concert.domain.entity.QConcertOptionEntity.concertOptionEntity
import dev.concert.domain.entity.QSeatEntity.seatEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.status.SeatStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport

interface ConcertSeatJpaRepository : JpaRepository<SeatEntity, Long>, ConcertSeatJpaRepositoryCustom {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatEntity s WHERE s.id = :seatId")
    fun findByIdWithLock(seatId: Long): SeatEntity?
}

interface ConcertSeatJpaRepositoryCustom {
    fun findAvailableSeats(concertOptionId: Long): List<SeatEntity>
}

class ConcertSeatJpaRepositoryImpl : ConcertSeatJpaRepositoryCustom, QuerydslRepositorySupport(SeatEntity::class.java) {
    override fun findAvailableSeats(concertOptionId: Long): List<SeatEntity> {
        return from(seatEntity)
            .join(seatEntity.concertOption, concertOptionEntity).fetchJoin()
            .join(concertOptionEntity.concert, concertEntity).fetchJoin()
            .where(
                seatEntity.concertOption.id.eq(concertOptionId),
                seatEntity.seatStatus.eq(SeatStatus.AVAILABLE)
            )
            .fetch()
    }
}