package org.ktor_lecture.concertservice.adapter.out.persistence.jpa

import com.querydsl.jpa.impl.JPAQueryFactory
import org.ktor_lecture.concertservice.domain.entity.QReservationEntity
import org.ktor_lecture.concertservice.domain.entity.QSeatEntity
import org.ktor_lecture.concertservice.domain.entity.ReservationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

interface ReservationJpaRepository: JpaRepository<ReservationEntity, Long>, ReservationRepositoryCustom {
}

interface ReservationRepositoryCustom {
    fun findReservationAndSeatInfo(reservationId: Long): ReservationEntity?
}

class ReservationJpaRepositoryImpl (
    private val queryFactory: JPAQueryFactory,
) : ReservationRepositoryCustom {

    override fun findReservationAndSeatInfo(reservationId: Long): ReservationEntity? {
        val reservation = QReservationEntity.reservationEntity
        val seat = QSeatEntity.seatEntity

        return queryFactory
            .selectFrom(reservation)
            .leftJoin(reservation.seat, seat).fetchJoin()
            .where(reservation.id.eq(reservationId))
            .fetchOne()
    }
}