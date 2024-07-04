package dev.concert.presentation

import dev.concert.ApiResult
import dev.concert.infrastructure.entity.status.QueueTokenStatus
import dev.concert.presentation.request.UserInfoRequest
import dev.concert.presentation.response.token.QueueTokenStatusResponse
import dev.concert.presentation.response.token.TokenResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/queue/token")
class TokenController {

    @GetMapping("/{token}/status")
    fun getTokenStatus(@PathVariable token: String) : ApiResult<QueueTokenStatusResponse> {
        return ApiResult(
            QueueTokenStatusResponse(
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
                status = QueueTokenStatus.ACTIVE,
                remainingTime = 360,
                queueOrder = 10
            )
        )
    }

    @PostMapping
    fun generateToken(
        @RequestBody userInfoRequest: UserInfoRequest,
    ): ApiResult<TokenResponse> {
        return ApiResult(TokenResponse(token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"))
    }
}