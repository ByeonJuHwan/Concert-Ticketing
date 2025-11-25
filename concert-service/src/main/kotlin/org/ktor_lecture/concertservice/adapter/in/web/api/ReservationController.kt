package org.ktor_lecture.concertservice.adapter.`in`.web.api

import org.ktor_lecture.concertservice.adapter.`in`.web.request.ChangeReservationPaidRequest
import org.ktor_lecture.concertservice.adapter.`in`.web.request.ChangeSeatReservedRequest
import org.ktor_lecture.concertservice.adapter.`in`.web.request.ReservationExpiredRequest
import org.ktor_lecture.concertservice.adapter.`in`.web.response.ConcertReservationResponse
import org.ktor_lecture.concertservice.application.port.`in`.ChangeSeatReservedUseCase
import org.ktor_lecture.concertservice.application.port.`in`.ReservationExpiredUseCase
import org.ktor_lecture.concertservice.application.port.`in`.ReservationPaidUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SearchReservationUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ReservationController(
    private val searchReservationUseCase: SearchReservationUseCase,
    private val reservationExpiredUseCase: ReservationExpiredUseCase,
    private val reservationPaidUseCase: ReservationPaidUseCase,
    private val changeSeatReservedUseCase: ChangeSeatReservedUseCase,
) {

    @GetMapping("/reservations/{reservationId}")
    fun getReservation(
        @PathVariable reservationId: Long,
    ): ConcertReservationResponse{
        return searchReservationUseCase.getReservation(reservationId)
    }

    @PostMapping("/reservations/expired")
    fun reservationExpiredAndSeatAvaliable(
        @RequestBody request: ReservationExpiredRequest
    ) {
        reservationExpiredUseCase.reservationExpiredAndSeatAvaliable(request.toCommand())
    }

    @PostMapping("/reservations/paid")
    fun changeReservationPaid(
        @RequestBody request: ChangeReservationPaidRequest
    ) {
        reservationPaidUseCase.changeReservationPaid(request.toCommand())
    }

    @PostMapping("reservations/seat/reserved")
    fun changeSeatStatus(
        @RequestBody request: ChangeSeatReservedRequest
    ) {
        changeSeatReservedUseCase.changeSeatStatusReserved(request.toCommand())
    }
}