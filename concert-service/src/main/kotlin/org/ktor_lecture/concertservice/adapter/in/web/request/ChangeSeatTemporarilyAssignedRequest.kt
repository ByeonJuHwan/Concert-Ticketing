package org.ktor_lecture.concertservice.adapter.`in`.web.request

import org.ktor_lecture.concertservice.application.service.command.ChangeReservationTemporarilyAssignedCommand

data class ChangeSeatTemporarilyAssignedRequest(
    val reservationId: Long,
) {
    fun toCommand() = ChangeReservationTemporarilyAssignedCommand(reservationId)
}
