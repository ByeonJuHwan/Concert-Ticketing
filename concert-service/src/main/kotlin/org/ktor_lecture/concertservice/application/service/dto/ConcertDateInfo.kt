package org.ktor_lecture.concertservice.application.service.dto

data class ConcertDateInfo(
    val concertId : Long,
    val concertName : String,
    val concertDate : String,
    val concertTime : String,
    val concertVenue : String,
    val availableSeats : Int,
)
