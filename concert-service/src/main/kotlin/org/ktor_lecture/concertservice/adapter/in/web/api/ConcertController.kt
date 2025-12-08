package org.ktor_lecture.concertservice.adapter.`in`.web.api

import org.ktor_lecture.concertservice.adapter.`in`.web.ApiResult
import org.ktor_lecture.concertservice.adapter.`in`.web.request.CreateConcertRequest
import org.ktor_lecture.concertservice.adapter.`in`.web.request.ReserveSeatRequest
import org.ktor_lecture.concertservice.adapter.`in`.web.response.*
import org.ktor_lecture.concertservice.application.port.`in`.CreateConcertUseCase
import org.ktor_lecture.concertservice.application.port.`in`.GetConcertSuggestionUseCase
import org.ktor_lecture.concertservice.application.port.`in`.ReserveSeatUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SearchAvailableDatesUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SearchAvailableSeatUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SearchConcertUseCase
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/concerts")
class ConcertController(
    private val searchConcertUseCase: SearchConcertUseCase,
    private val searchAvailableDatesUseCase: SearchAvailableDatesUseCase,
    private val searchAvailableSeatUseCase: SearchAvailableSeatUseCase,
    private val reserveSeatUseCase: ReserveSeatUseCase,
    private val createConcertUseCase: CreateConcertUseCase,
    private val getConcertSuggestionUseCase: GetConcertSuggestionUseCase,
) {

    @GetMapping
    fun getConcerts(
        @RequestParam(required = false) concertName: String?,
        @RequestParam(required = false) singer: String?,
        @RequestParam(required = false) startDate: LocalDate?,
        @RequestParam(required = false) endDate: LocalDate?,
    ): ApiResult<List<ConcertsResponse>> {
        val concerts = searchConcertUseCase.getConcerts(
            concertName,
            singer,
            startDate,
            endDate
        )
        return ApiResult(data = ConcertsResponse.fromList(concerts))
    }


    @GetMapping("/{concertId}/available-dates")
    fun getAvailableDates(
        @PathVariable concertId: Long,
    ): ApiResult<List<ConcertAvailableDatesResponse>> {
        val dates = searchAvailableDatesUseCase.getAvailableDates(concertId)
        return ApiResult(data = ConcertAvailableDatesResponse.fromList(dates))
    }


    @GetMapping("/{concertOptionId}/available-seats")
    fun getAvailableSeats(
        @PathVariable concertOptionId: Long,
    ): ApiResult<ConcertAvailableSeatsResponse> {
        val seats = searchAvailableSeatUseCase.getAvailableSeats(concertOptionId)
        return ApiResult(data = ConcertAvailableSeatsResponse(concertOptionId, ConcertSeatInfoResponse.fromList(seats)))
    }

    @PostMapping("/reserve-seat")
    fun reserveSeat(
        @RequestBody request: ReserveSeatRequest
    ): ApiResult<ConcertReservationStatusResponse> {
        val response = reserveSeatUseCase.reserveSeat(request.toCommand())
        return ApiResult(ConcertReservationStatusResponse.from(response))
    }

    @PostMapping
    fun createConcert(
        @RequestBody request: CreateConcertRequest,
    ) {
        createConcertUseCase.createConcert(request.toCommand())
    }

    @GetMapping("/suggestions")
    fun getConcertSuggestions(
        @RequestParam query: String,
    ): ApiResult<List<String>> {
        val suggestions = getConcertSuggestionUseCase.getConcertSuggestions(query)
        return ApiResult(suggestions)
    }
}