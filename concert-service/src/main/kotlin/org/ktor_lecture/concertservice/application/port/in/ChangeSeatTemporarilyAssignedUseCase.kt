package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.application.service.command.ChangeReservationTemporarilyAssignedCommand

interface ChangeSeatTemporarilyAssignedUseCase {
    fun changeSeatTemporarilyAssigned(command: ChangeReservationTemporarilyAssignedCommand)
}