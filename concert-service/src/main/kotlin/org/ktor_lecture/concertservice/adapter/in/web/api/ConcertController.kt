package org.ktor_lecture.concertservice.adapter.`in`.web.api

import org.ktor_lecture.concertservice.adapter.`in`.web.ApiResult
import org.ktor_lecture.concertservice.adapter.`in`.web.request.CreateConcertRequest
import org.ktor_lecture.concertservice.adapter.`in`.web.request.ReserveSeatRequest
import org.ktor_lecture.concertservice.adapter.`in`.web.response.*
import org.ktor_lecture.concertservice.application.port.`in`.CreateConcertUseCase
import org.ktor_lecture.concertservice.application.port.`in`.ReserveSeatUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SearchAvailableDatesUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SearchAvailableSeatUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SearchConcertUseCase
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/concerts")
class ConcertController (
    private val searchConcertUseCase: SearchConcertUseCase,
    private val searchAvailableDatesUseCase: SearchAvailableDatesUseCase,
    private val searchAvailableSeatUseCase: SearchAvailableSeatUseCase,
    private val reserveSeatUseCase: ReserveSeatUseCase,
    private val createConcertUseCase: CreateConcertUseCase,
): ConcertControllerSwagger {

    @GetMapping
    override fun getConcerts(): ApiResult<List<ConcertsResponse>> {
        val concerts = searchConcertUseCase.getConcerts()
        return ApiResult(data = ConcertsResponse.fromList(concerts))
    }


    @GetMapping("/{concertId}/available-dates")
    override fun getAvailableDates(
        @PathVariable concertId: Long,
    ): ApiResult<List<ConcertAvailableDatesResponse>> {
        val dates = searchAvailableDatesUseCase.getAvailableDates(concertId)
        return ApiResult(data = ConcertAvailableDatesResponse.fromList(dates))
    }


    @GetMapping("/{concertOptionId}/available-seats")
    override fun getAvailableSeats(
        @PathVariable concertOptionId: Long,
    ): ApiResult<ConcertAvailableSeatsResponse> {
        val seats = searchAvailableSeatUseCase.getAvailableSeats(concertOptionId)
        return ApiResult(data = ConcertAvailableSeatsResponse(concertOptionId, ConcertSeatInfoResponse.fromList(seats)))
    }

    @PostMapping("/reserve-seat")
    override fun reserveSeat(
        @RequestBody request: ReserveSeatRequest
    ) : ApiResult<ConcertReservationStatusResponse> {
        val response = reserveSeatUseCase.reserveSeat(request.toCommand())
        return ApiResult(ConcertReservationStatusResponse.from(response))
    }

    @PostMapping
    fun createConcert(
        @RequestBody request: CreateConcertRequest,
    ) {
        createConcertUseCase.createConcert(request.toCommand())
    }
}