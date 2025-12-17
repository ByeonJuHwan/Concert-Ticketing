package org.ktor_lecture.concertservice.application.service.command

data class ChangeReservationTemporarilyAssignedCommand(
    val reservationId: Long,
)
