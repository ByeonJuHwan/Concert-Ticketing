package dev.concert.presentation

import dev.concert.ApiResult
import dev.concert.application.concert.facade.ConcertFacade
import dev.concert.presentation.response.concert.ConcertAvailableDatesResponse
import dev.concert.presentation.response.concert.ConcertAvailableSeatsResponse
import dev.concert.presentation.response.concert.ConcertsResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/concerts")
class ConcertController (
    private val concertFacade: ConcertFacade
) {

    @GetMapping
    fun getConcerts(): ApiResult<ConcertsResponse> {
        val concerts = concertFacade.getConcerts()
        return ApiResult(data = ConcertsResponse(concerts))
    }

    @Operation(summary = "콘서트 날짜 조회 API", description = "콘서트의 예약 가능한 날짜를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "콘서트 날짜 조회 성공"),
    )
    @GetMapping("/{concertId}/available-dates")
    fun getAvailableDates(
        @PathVariable concertId: Long,
    ): ApiResult<ConcertAvailableDatesResponse> {
        val dates = concertFacade.getAvailableDates(concertId)
        return ApiResult(data = ConcertAvailableDatesResponse(dates))
    }

    @Operation(summary = "콘서트 예악 가능 좌석 조회 API", description = "콘서트의 예약 가능한 좌석을 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "콘서트 좌석 조회 성공"),
    )
    @GetMapping("/{concertOptionId}/available-seats")
    fun getAvailableSeats(
        @PathVariable concertOptionId: Long,
    ): ApiResult<ConcertAvailableSeatsResponse> {
        val seats = concertFacade.getAvailableSeats(concertOptionId)
        return ApiResult(data = ConcertAvailableSeatsResponse(concertOptionId, seats))
    }
}