package org.ktor_lecture.concertservice.adapter.`in`.web.request

import org.ktor_lecture.concertservice.application.service.command.ReserveSeatCommand

data class ReserveSeatRequest(
    val seatId : Long,
    val userId  : Long,
) {
    fun toCommand(): ReserveSeatCommand {
        return ReserveSeatCommand(
            userId = userId,
            seatId = seatId,
        )
    }
}