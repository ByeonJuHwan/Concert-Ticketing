package org.ktor_lecture.concertservice.adapter.`in`.web.api.internal

import org.ktor_lecture.concertservice.adapter.`in`.web.response.ConcertUserReservationsResponse
import org.ktor_lecture.concertservice.application.service.ConcertReadService
import org.ktor_lecture.concertservice.application.service.ConcertReservationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/concerts")
class ReservationInternalController (
    private val concertReservationService: ConcertReservationService,
    private val concertReadService: ConcertReadService,
) {

    @GetMapping("/reservations/{userId}")
    fun searchReservations(@PathVariable userId: Long): List<ConcertUserReservationsResponse> {
        return concertReservationService.searchUserReservations(userId)
    }

    @GetMapping
    fun searchConcerts(): List<SearchConcertsResponse> {
        val concerts = concertReadService.getConcerts(
            concertName = null,
            singer = null,
            startDate = null,
            endDate = null
        )

        return concerts.map {
            SearchConcertsResponse(
                concertId = it.id,
                concertName = it.concertName,
                concertStartDate = it.startDate,
                concertEndDate = it.endDate,
            )
        }
    }
}

data class SearchConcertsResponse(
    val concertId: Long,
    val concertName: String,
    val concertStartDate: String,
    val concertEndDate: String,
)