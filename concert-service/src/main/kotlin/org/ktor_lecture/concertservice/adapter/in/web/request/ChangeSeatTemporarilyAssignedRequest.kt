package org.ktor_lecture.concertservice.adapter.`in`.web.request

import org.ktor_lecture.concertservice.application.service.command.ChangeReservationTemporarilyAssignedCommand

data class ChangeSeatTemporarilyAssignedRequest(
    val requestId: String,
) {
    fun toCommand() = ChangeReservationTemporarilyAssignedCommand(requestId)
}
