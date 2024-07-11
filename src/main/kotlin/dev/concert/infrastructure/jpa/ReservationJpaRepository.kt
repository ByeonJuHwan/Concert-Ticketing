package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.QConcertEntity.concertEntity
import dev.concert.domain.entity.QConcertOptionEntity.concertOptionEntity
import dev.concert.domain.entity.QReservationEntity.reservationEntity
import dev.concert.domain.entity.QSeatEntity.seatEntity
import dev.concert.domain.entity.QUserEntity.userEntity
import dev.concert.domain.entity.ReservationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import java.time.LocalDateTime.now

interface ReservationJpaRepository : JpaRepository<ReservationEntity, Long>, ReservationJpaRepositoryCustom

interface ReservationJpaRepositoryCustom {
    fun findReservationInfo(reservationId: Long): ReservationEntity?
    fun findExpiredReservations(): List<ReservationEntity>
}

class ReservationJpaRepositoryImpl : ReservationJpaRepositoryCustom, QuerydslRepositorySupport(ReservationEntity::class.java) {
    override fun findReservationInfo(reservationId: Long): ReservationEntity? {
        return from(reservationEntity)
            .join(reservationEntity.seat, seatEntity).fetchJoin()
            .join(reservationEntity.user, userEntity).fetchJoin()
            .join(seatEntity.concertOption, concertOptionEntity).fetchJoin()
            .join(concertOptionEntity.concert, concertEntity).fetchJoin()
            .where(reservationEntity.id.eq(reservationId))
            .fetchOne()
    }

    override fun findExpiredReservations(): List<ReservationEntity> {
        return from(reservationEntity)
            .join(reservationEntity.seat, seatEntity).fetchJoin()
            .join(reservationEntity.user, userEntity).fetchJoin()
            .join(seatEntity.concertOption, concertOptionEntity).fetchJoin()
            .join(concertOptionEntity.concert, concertEntity).fetchJoin()
            .where(reservationEntity.expiresAt.lt(now()))
            .fetch()
    }
}