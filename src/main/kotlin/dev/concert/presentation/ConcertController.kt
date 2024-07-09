package dev.concert.presentation

import dev.concert.ApiResult
import dev.concert.application.concert.facade.ConcertFacade
import dev.concert.presentation.response.concert.ConcertsResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
}