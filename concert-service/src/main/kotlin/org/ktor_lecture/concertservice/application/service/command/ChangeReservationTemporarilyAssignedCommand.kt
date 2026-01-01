package org.ktor_lecture.concertservice.application.service.command

data class ChangeReservationTemporarilyAssignedCommand(
    val sagaId: String,
    val reservationId: Long,
)
