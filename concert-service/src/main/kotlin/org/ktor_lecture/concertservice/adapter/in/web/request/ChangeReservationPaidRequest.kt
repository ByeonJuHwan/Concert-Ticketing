package org.ktor_lecture.concertservice.adapter.`in`.web.request

import org.ktor_lecture.concertservice.application.service.command.ReservationPaidCommand

data class ChangeReservationPaidRequest (
    val requestId: String,
) {
    fun toCommand() = ReservationPaidCommand (
        requestId = requestId,
    )
}