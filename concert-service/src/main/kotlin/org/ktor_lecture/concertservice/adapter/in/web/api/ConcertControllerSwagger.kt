package org.ktor_lecture.concertservice.adapter.`in`.web.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.ktor_lecture.concertservice.adapter.`in`.web.ApiResult
import org.ktor_lecture.concertservice.adapter.`in`.web.request.ReserveSeatRequest
import org.ktor_lecture.concertservice.adapter.`in`.web.response.ConcertAvailableDatesResponse
import org.ktor_lecture.concertservice.adapter.`in`.web.response.ConcertAvailableSeatsResponse
import org.ktor_lecture.concertservice.adapter.`in`.web.response.ConcertReservationResponse
import org.ktor_lecture.concertservice.adapter.`in`.web.response.ConcertsResponse
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

interface ConcertControllerSwagger {

    @Operation(summary = "콘서트 목록 조회 API", description = "콘서트 목록을 조회합니다")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "콘서트 목록 조회 성공"),
    )
    fun getConcerts(): ApiResult<List<ConcertsResponse>>

    @Operation(summary = "콘서트 날짜 조회 API", description = "콘서트의 예약 가능한 날짜를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "콘서트 날짜 조회 성공"),
    )
    fun getAvailableDates(@PathVariable concertId: Long,): ApiResult<List<ConcertAvailableDatesResponse>>


    @Operation(summary = "콘서트 예악 가능 좌석 조회 API", description = "콘서트의 예약 가능한 좌석을 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "콘서트 좌석 조회 성공"),
    )
    fun getAvailableSeats(
        @PathVariable concertOptionId: Long,
    ): ApiResult<ConcertAvailableSeatsResponse>


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
    fun reserveSeat(
        @RequestBody request: ReserveSeatRequest
    ) : ApiResult<ConcertReservationResponse>
}
