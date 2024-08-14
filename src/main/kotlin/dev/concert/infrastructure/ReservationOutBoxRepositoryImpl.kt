package dev.concert.infrastructure

import dev.concert.domain.entity.outbox.ReservationEventOutBox
import dev.concert.domain.entity.status.OutBoxMsgStats.*
import dev.concert.domain.repository.ReservationOutBoxRepository
import dev.concert.infrastructure.jpa.ReservationOutBoxJpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

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

    @Transactional
    override fun updateStatusSuccess(outboxEntity: ReservationEventOutBox) {
        reservationOutBoxJpaRepository.updateByReservationId(outboxEntity.reservationId, SEND_SUCCESS)
    }

    @Transactional
    override fun updateStatusFail(outboxEntity: ReservationEventOutBox) {
        reservationOutBoxJpaRepository.updateByReservationId(outboxEntity.reservationId, SEND_FAIL)
    }

    override fun getInitOrFailEvents(): List<ReservationEventOutBox> {
        return reservationOutBoxJpaRepository.findEventByStatuses(listOf(SEND_FAIL, INIT), LocalDateTime.now().minusMinutes(10))
    }

    override fun deleteEntriesOlderThanThreeDays() {
        reservationOutBoxJpaRepository.deleteEntriesOlderThan(LocalDateTime.now().minusDays(3))
    }
}