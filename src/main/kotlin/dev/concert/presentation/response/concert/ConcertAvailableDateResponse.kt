package dev.concert.presentation.response.concert

data class ConcertAvailableDate(
    val concertId : Long,
    val title : String,
    val concertDate : String,
    val concertTime : String,
    val concertVenue : String,
    val availableSeats : Int,
)

data class ConcertAvailableDateResponse(
    val concerts: List<ConcertAvailableDate>,
)
