package dev.concert.domain.repository

import dev.concert.domain.entity.outbox.ReservationEventOutBox

interface ReservationOutBoxRepository {
    fun save(outboxEntity : ReservationEventOutBox) : ReservationEventOutBox
    fun findByReservationId(reservationId: Long) : ReservationEventOutBox?
    fun updateStatusSuccess(outboxEntity: ReservationEventOutBox)
    fun updateStatusFail(outboxEntity: ReservationEventOutBox)
    fun updateCreatedAt11MinutesAgo(outboxEntity: ReservationEventOutBox)
    fun getInitOrFailEvents(): List<ReservationEventOutBox>
    fun deleteEntriesOlderThanThreeDays()
}