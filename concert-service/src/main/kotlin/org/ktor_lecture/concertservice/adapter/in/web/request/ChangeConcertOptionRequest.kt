package org.ktor_lecture.concertservice.adapter.`in`.web.request

import org.ktor_lecture.concertservice.application.service.command.ChangeConcertOptionCommand

data class ChangeConcertOptionRequest (
    val availableSeats : Int,
    val concertDate: String,
    val concertTime: String,
    val concertVenue: String,
) {
    fun toCommand() = ChangeConcertOptionCommand(
        availableSeats = availableSeats,
        concertDate = concertDate,
        concertTime = concertTime,
        concertVenue = concertVenue,
    )
}