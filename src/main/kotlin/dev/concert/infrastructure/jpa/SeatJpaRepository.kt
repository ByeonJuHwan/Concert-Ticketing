package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.QConcertEntity.concertEntity
import dev.concert.domain.entity.QConcertOptionEntity.concertOptionEntity
import dev.concert.domain.entity.QSeatEntity.seatEntity
import dev.concert.domain.entity.SeatEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport

interface SeatJpaRepository : JpaRepository<SeatEntity, Long>, SeatJpaRepositoryCustom

interface SeatJpaRepositoryCustom {
    fun getSeatWithLock(seatId: Long): SeatEntity?
}

class SeatJpaRepositoryImpl : SeatJpaRepositoryCustom, QuerydslRepositorySupport(SeatEntity::class.java){

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun getSeatWithLock(seatId: Long): SeatEntity? {
        return from(seatEntity)
            .join(seatEntity.concertOption, concertOptionEntity)
            .join(concertOptionEntity.concert, concertEntity)
            .fetchJoin()
            .where(seatEntity.id.eq(seatId))
            .fetchOne()
    }
}