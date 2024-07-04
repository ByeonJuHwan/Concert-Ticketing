package dev.concert.presentation.response.concert

data class ConcertAvailableSeatsResponse (
    val concertOptionId : Long,
    val seats : List<ConcertSeat>
)

data class ConcertSeat(
    val seatId : Long,
    val seatNumber : Int,
    val price : Long,
)