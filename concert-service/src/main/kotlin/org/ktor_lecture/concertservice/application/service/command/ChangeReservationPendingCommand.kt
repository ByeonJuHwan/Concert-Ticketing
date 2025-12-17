package org.ktor_lecture.concertservice.application.service.command

data class ChangeReservationPendingCommand(
    val reservationId: Long,
)
