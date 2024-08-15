package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.outbox.ReservationEventOutBox
import dev.concert.domain.entity.status.OutBoxMsgStats
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ReservationOutBoxJpaRepository : JpaRepository<ReservationEventOutBox, Long> {
    fun findByReservationId(reservationId: Long): ReservationEventOutBox?

    @Query("SELECT r FROM ReservationEventOutBox r WHERE r.status IN :statuses AND r.createdAt < :tenMinutesAgo")
    fun findEventByStatuses(@Param("statuses") statuses: List<OutBoxMsgStats>, @Param("tenMinutesAgo") tenMinutesAgo : LocalDateTime): List<ReservationEventOutBox>

    @Modifying
    @Query("UPDATE ReservationEventOutBox SET status = :status WHERE reservationId = :reservationId")
    fun updateByReservationId(@Param(value = "reservationId") reservationId: Long, @Param(value = "status") outBoxMsgStats: OutBoxMsgStats)

    @Modifying
    @Query("DELETE FROM ReservationEventOutBox r WHERE r.createdAt < :date")
    fun deleteEntriesOlderThan(date: LocalDateTime)

    @Modifying
    @Query("UPDATE ReservationEventOutBox SET createdAt = :time WHERE reservationId = :reservationId")
    fun updateCreatedAt11MinutesAgo(@Param(value = "reservationId") reservationId: Long, @Param(value = "time") time: LocalDateTime)
}