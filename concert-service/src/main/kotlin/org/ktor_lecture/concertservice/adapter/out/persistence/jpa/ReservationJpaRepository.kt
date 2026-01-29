package org.ktor_lecture.concertservice.adapter.out.persistence.jpa

import com.querydsl.jpa.impl.JPAQueryFactory
import org.ktor_lecture.concertservice.domain.entity.QConcertUserEntity
import org.ktor_lecture.concertservice.domain.entity.QReservationEntity
import org.ktor_lecture.concertservice.domain.entity.QSeatEntity
import org.ktor_lecture.concertservice.domain.entity.ReservationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Component
import java.time.LocalDateTime

interface ReservationJpaRepository: JpaRepository<ReservationEntity, Long>, ReservationRepositoryCustom {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ReservationEntity r SET r.status = 'EXPIRED' WHERE r.id IN :reservationIds")
    fun updateReservationStatusToExpired(reservationIds: List<Long>)
}

interface ReservationRepositoryCustom {
    fun findReservationAndSeatInfo(reservationId: Long): ReservationEntity?
    fun findExpiredReservations(): List<ReservationEntity>
    fun searchUserReservations(userId: Long): List<ReservationEntity>
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

    override fun findExpiredReservations(): List<ReservationEntity> {
        val reservation = QReservationEntity.reservationEntity
        val seat = QSeatEntity.seatEntity
        val now = LocalDateTime.now()

        return queryFactory
            .selectFrom(reservation)
            .join(reservation.seat, seat).fetchJoin()
            .where(reservation.expiresAt.lt(now))
            .fetch()
    }

    override fun searchUserReservations(userId: Long): List<ReservationEntity> {
        val reservation = QReservationEntity.reservationEntity
        val concertUser = QConcertUserEntity.concertUserEntity
        val seat = QSeatEntity.seatEntity

        return queryFactory
            .selectFrom(reservation)
            .innerJoin(reservation.user, concertUser).fetchJoin()
            .leftJoin(reservation.seat, seat).fetchJoin()
            .where(reservation.user.id.eq(userId))
            .fetch()
    }
}