package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.outbox.ReservationEventOutBox
import dev.concert.domain.entity.status.OutBoxMsgStats
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ReservationOutBoxJpaRepository : JpaRepository<ReservationEventOutBox, Long> {
    fun findByReservationId(reservationId: Long): ReservationEventOutBox?

    @Query("SELECT r FROM ReservationEventOutBox r WHERE r.status IN :statuses")
    fun findEventByStatuses(@Param("statuses") statuses: List<OutBoxMsgStats>): List<ReservationEventOutBox>

    @Modifying
    @Query("UPDATE ReservationEventOutBox SET status = :status WHERE reservationId = :reservationId")
    fun updateByReservationId(@Param(value = "reservationId") reservationId: Long, @Param(value = "status") outBoxMsgStats: OutBoxMsgStats)
}