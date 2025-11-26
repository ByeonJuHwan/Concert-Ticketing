package org.ktor_lecture.concertservice.adapter.`in`.web.request

import org.ktor_lecture.concertservice.application.service.command.ReservationExpiredCommand

data class ReservationExpiredRequest(
    val reservationId: Long,
) {
    fun toCommand(): ReservationExpiredCommand {
        return ReservationExpiredCommand(reservationId)
    }
}