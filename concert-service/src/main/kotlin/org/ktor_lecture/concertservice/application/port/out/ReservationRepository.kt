package org.ktor_lecture.concertservice.application.port.out

import org.ktor_lecture.concertservice.domain.entity.ReservationEntity

interface ReservationRepository {
    fun save(reservation: org.ktor_lecture.concertservice.domain.entity.ReservationEntity)
}