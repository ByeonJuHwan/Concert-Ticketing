package dev.concert.infrastructure

import dev.concert.domain.entity.outbox.ReservationEventOutBox
import dev.concert.domain.entity.status.OutBoxMsgStats
import dev.concert.domain.repository.ReservationOutBoxRepository
import dev.concert.infrastructure.jpa.ReservationOutBoxJpaRepository
import org.springframework.stereotype.Repository

@Repository
class ReservationOutBoxRepositoryImpl (
    private val reservationOutBoxJpaRepository: ReservationOutBoxJpaRepository
): ReservationOutBoxRepository {
    override fun save(outboxEntity: ReservationEventOutBox) {
        reservationOutBoxJpaRepository.save(outboxEntity)
    }

    override fun findByReservationId(reservationId: Long) : ReservationEventOutBox? {
        return reservationOutBoxJpaRepository.findByReservationId(reservationId)
    }

    override fun updateStatusSuccess(outboxEntity: ReservationEventOutBox) {
        reservationOutBoxJpaRepository.updateByReservationId(outboxEntity.reservationId, OutBoxMsgStats.SEND_SUCCESS)
    }
}