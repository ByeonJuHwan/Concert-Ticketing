package dev.concert.application.concert.dto

data class ConcertDatesDto(
    val concertId : Long,
    val concertName : String,
    val concertDate : String,
    val concertTime : String,
    val concertVenue : String,
    val availableSeats : Int,
)
