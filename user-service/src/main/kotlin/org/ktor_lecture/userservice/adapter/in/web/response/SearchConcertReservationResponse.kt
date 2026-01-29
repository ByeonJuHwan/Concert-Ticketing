package org.ktor_lecture.userservice.adapter.`in`.web.response

data class SearchConcertReservationResponse(
    val userId: Long,
    val reservationHistories: List<ReservationHistoryResponse>,
    val concertList: List<ConcertResponse>,
)

data class ReservationHistoryResponse(
    val reservationId: Long,
    val reservationStatus: String,
    val seatId: Long,
    val price:Long,
)

data class ConcertResponse (
    val concertId: Long,
    val concertName: String,
    val concertStartDate: String,
    val concertEndDate: String,
)