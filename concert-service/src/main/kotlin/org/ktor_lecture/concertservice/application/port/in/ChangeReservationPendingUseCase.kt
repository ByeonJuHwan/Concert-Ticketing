package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.application.service.command.ChangeReservationPendingCommand

interface ChangeReservationPendingUseCase {
    fun changeReservationPending(command: ChangeReservationPendingCommand)

}