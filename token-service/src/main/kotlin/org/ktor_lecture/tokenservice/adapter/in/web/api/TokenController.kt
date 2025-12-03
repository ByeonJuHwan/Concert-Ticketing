package org.ktor_lecture.tokenservice.adapter.`in`.web.api

import org.ktor_lecture.tokenservice.adapter.`in`.web.ApiResult
import org.ktor_lecture.tokenservice.adapter.`in`.web.request.TokenRequest
import org.ktor_lecture.tokenservice.adapter.`in`.web.response.TokenInfoResponse
import org.ktor_lecture.tokenservice.adapter.`in`.web.response.TokenResponse
import org.ktor_lecture.tokenservice.application.port.`in`.CreateTokenUseCase
import org.ktor_lecture.tokenservice.application.port.`in`.GetTokenStatusUseCase
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/queue/tokens")
class TokenController (
    private val createTokenUseCase: CreateTokenUseCase,
    private val getTokenStatusUseCase: GetTokenStatusUseCase,
) {

    @PostMapping 
    fun createToken( 
        @RequestBody request: TokenRequest
    ) : ApiResult<TokenResponse> {
        val response = createTokenUseCase.createToken(request.toCommand())
        return ApiResult(
            data = response,
            status = HttpStatus.CREATED.value(),
            message = "대기열 토큰 발행 성공"
        )
    } 

    @GetMapping("/status/{userId}")
    fun getToken(
        @PathVariable userId: Long,
    ) : ApiResult<TokenInfoResponse> {
        val response = getTokenStatusUseCase.getToken(userId)
        return ApiResult(
            data = response,
            message = "대기열 토큰 상태 조회 성공"
        )
    }
}
