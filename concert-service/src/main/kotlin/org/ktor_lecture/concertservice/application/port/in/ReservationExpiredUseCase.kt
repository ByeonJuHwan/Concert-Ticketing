package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.application.service.command.ReservationExpiredCommand

interface ReservationExpiredUseCase {
    fun reservationExpiredAndSeatAvaliable(request: ReservationExpiredCommand)
}