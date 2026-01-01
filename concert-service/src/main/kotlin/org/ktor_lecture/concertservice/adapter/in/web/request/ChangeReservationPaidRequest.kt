package org.ktor_lecture.concertservice.adapter.`in`.web.request

import org.ktor_lecture.concertservice.application.service.command.ReservationPaidCommand

data class ChangeReservationPaidRequest (
    val reservationId: Long,
) {
    fun toCommand() = ReservationPaidCommand (
        reservationId = reservationId,
    )
}