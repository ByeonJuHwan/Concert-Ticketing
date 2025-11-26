package org.ktor_lecture.concertservice.adapter.`in`.web.request

import org.ktor_lecture.concertservice.application.service.command.ChangeReservationPendingCommand

data class ChangeReservationPendingRequest (
    val requestId: String,
) {
    fun toCommand() = ChangeReservationPendingCommand(requestId)
}