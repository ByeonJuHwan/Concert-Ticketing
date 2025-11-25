package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.application.service.command.ReservationPaidCommand

interface ReservationPaidUseCase {
    fun changeReservationPaid(command: ReservationPaidCommand)
}