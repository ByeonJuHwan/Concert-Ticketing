package dev.concert.presentation

import dev.concert.ApiResult
import dev.concert.application.concert.facade.ConcertFacade
import dev.concert.presentation.response.concert.ConcertsResponse
import org.springframework.web.bind.annotation.GetMapping
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
}