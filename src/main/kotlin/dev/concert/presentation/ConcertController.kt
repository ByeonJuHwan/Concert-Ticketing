package dev.concert.presentation

import dev.concert.ApiResult
import dev.concert.application.concert.facade.ConcertFacade
import dev.concert.presentation.response.concert.ConcertAvailableDatesResponse
import dev.concert.presentation.response.concert.ConcertAvailableSeatsResponse
import dev.concert.presentation.request.ReserveSeatRequest
import dev.concert.presentation.request.toDto
import dev.concert.presentation.response.concert.ConcertReservationResponse
import dev.concert.presentation.response.concert.ConcertsResponse
import dev.concert.presentation.response.concert.toResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "콘서트 API", description = "콘서트 예약 API")
@RestController
@RequestMapping("/concerts")
class ConcertController (
    private val concertFacade: ConcertFacade
) {
    @Operation(summary = "콘서트 목록 조회 API", description = "콘서트 목록을 조회합니다")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "콘서트 목록 조회 성공"),
    )
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

    @Operation(summary = "콘서트 좌석 예약 API", description = "콘서트 좌석을 예약합니다") 
    @ApiResponses( 
        ApiResponse(responseCode = "200", description = "콘서트 좌석 예약 성공"), 
        ApiResponse( 
            responseCode = "404", 
            description = "존재하는 좌석이 없습니다", 
            content = [Content( 
                mediaType = "application/json", 
                examples = [ExampleObject( 
                    value = """ 
                        { 
                            "code": "404", 
                            "message": "존재하는 좌석이 없습니다" 
                        } 
                    """ 
                )] 
            )] 
        ), 
        ApiResponse( 
            responseCode = "409", 
            description = "예약 가능한 상태가 아닙니다", 
            content = [Content( 
                mediaType = "application/json",
                examples = [ExampleObject( 
                    value = """ 
                        { 
                            "code": "409", 
                            "message": "예약 가능한 상태가 아닙니다" 
                        } 
                    """ 
                )] 
            )] 
        ), 
    ) 
    @PostMapping("reserve-seat") 
    fun reserveSeat( 
        @RequestBody request: ReserveSeatRequest 
    ) : ApiResult<ConcertReservationResponse>{ 
        return ApiResult(data = concertFacade.reserveSeat(request.toDto()).toResponse()) 
    } 
}
