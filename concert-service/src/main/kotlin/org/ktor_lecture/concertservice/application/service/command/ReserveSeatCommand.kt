package org.ktor_lecture.concertservice.application.service.command

data class ReserveSeatCommand(
    val seatId: Long,
    val userId: Long,
)

data class ChangeSeatStatusReservedCommand(
    val requestId: String,
)