package org.ktor_lecture.concertservice.adapter.`in`.web.request

import org.ktor_lecture.concertservice.application.service.command.ReserveSeatCommand

data class ReserveSeatRequest(
    val seatId : Long,
    val userId  : Long,
)

fun ReserveSeatRequest.toCommand() = ReserveSeatCommand(
    seatId = seatId,
    userId = userId,
)
