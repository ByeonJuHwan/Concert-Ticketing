package org.ktor_lecture.concertservice.application.service.command

data class ChangeReservationPendingCommand(
    val sagaId: String,
    val reservationId: Long,
)
