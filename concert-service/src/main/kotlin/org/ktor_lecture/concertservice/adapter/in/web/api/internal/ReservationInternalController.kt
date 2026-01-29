package org.ktor_lecture.concertservice.adapter.`in`.web.api.internal

import org.ktor_lecture.concertservice.adapter.`in`.web.response.ConcertUserReservationsResponse
import org.ktor_lecture.concertservice.application.service.ConcertReservationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/concerts")
class ReservationInternalController (
    private val concertReservationService: ConcertReservationService,
) {

    @GetMapping("/reservations/{userId}")
    fun searchReservations(@PathVariable userId: Long): List<ConcertUserReservationsResponse> {
        return concertReservationService.searchUserReservations(userId)
    }
}