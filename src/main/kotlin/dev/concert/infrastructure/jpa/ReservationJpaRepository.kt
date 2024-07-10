package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.QReservationEntity.reservationEntity
import dev.concert.domain.entity.QSeatEntity.seatEntity
import dev.concert.domain.entity.QUserEntity.userEntity
import dev.concert.domain.entity.ReservationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport

interface ReservationJpaRepository : JpaRepository<ReservationEntity, Long>, ReservationJpaRepositoryCustom

interface ReservationJpaRepositoryCustom {
    fun findReservationInfo(reservationId: Long): ReservationEntity?
}

class ReservationJpaRepositoryImpl : ReservationJpaRepositoryCustom, QuerydslRepositorySupport(ReservationEntity::class.java) {
    override fun findReservationInfo(reservationId: Long): ReservationEntity? {
        return from(reservationEntity)
            .join(reservationEntity.seat, seatEntity)
            .join(reservationEntity.user, userEntity)
            .fetchJoin()
            .where(reservationEntity.id.eq(reservationId))
            .fetchOne()
    }
}