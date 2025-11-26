package org.ktor_lecture.concertservice.application.service.command

data class ReservationExpiredCommand (
    val reservationId : Long,
)