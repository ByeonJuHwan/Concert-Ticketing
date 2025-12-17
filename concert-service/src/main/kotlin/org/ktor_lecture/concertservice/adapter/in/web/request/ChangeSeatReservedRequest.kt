package org.ktor_lecture.concertservice.adapter.`in`.web.request

import org.ktor_lecture.concertservice.application.service.command.ChangeSeatStatusReservedCommand

data class ChangeSeatReservedRequest(
    val reservationId: Long,
) {
    fun toCommand() = ChangeSeatStatusReservedCommand(
        reservationId = reservationId
    )
}
