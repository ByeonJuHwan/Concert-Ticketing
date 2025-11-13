package org.ktor_lecture.concertservice.adapter.out.persistence

import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.ReservationJpaRepository
import org.ktor_lecture.concertservice.application.port.out.ReservationRepository
import org.ktor_lecture.concertservice.domain.entity.ReservationEntity
import org.springframework.stereotype.Component

@Component
class ReservationWriteAdapter (
    private val reservationJpaRepository: ReservationJpaRepository,
): ReservationRepository {
    override fun save(reservation: ReservationEntity) {
        reservationJpaRepository.save(reservation)
    }
}