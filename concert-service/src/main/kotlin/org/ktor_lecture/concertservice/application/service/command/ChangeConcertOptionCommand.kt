package org.ktor_lecture.concertservice.application.service.command

data class ChangeConcertOptionCommand(
    val availableSeats : Int,
    val concertDate: String,
    val concertTime: String,
    val concertVenue: String,
)
