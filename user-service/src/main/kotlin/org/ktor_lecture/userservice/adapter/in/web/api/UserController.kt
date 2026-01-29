package org.ktor_lecture.userservice.adapter.`in`.web.api

import org.ktor_lecture.userservice.adapter.`in`.web.ApiResult
import org.ktor_lecture.userservice.adapter.`in`.web.request.CreateUserRequest
import org.ktor_lecture.userservice.adapter.`in`.web.request.toCommand
import org.ktor_lecture.userservice.adapter.`in`.web.response.SearchPaymentHistoryResponse
import org.ktor_lecture.userservice.application.port.`in`.CreateUserUseCase
import org.ktor_lecture.userservice.application.port.`in`.SearchPaymentHistoryUseCase
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController (
    private val createUserUseCase: CreateUserUseCase,
    private val searchPaymentHistoryUseCase: SearchPaymentHistoryUseCase,
) {

    @PostMapping("/api/v1/user")
    fun createUser(
        @RequestBody request: CreateUserRequest,
    ): ApiResult<Unit> {
        createUserUseCase.createUser(request.toCommand())
        return ApiResult(
            data = Unit,
            status = HttpStatus.CREATED.value(),
            message = "유저 회원가입 성공"
        )
    }

    @GetMapping("/api/v1/user/{userId}/payment-detail-history")
    fun searchGrpcPaymentDetailHistory(@PathVariable userId: Long): ApiResult<SearchPaymentHistoryResponse> {
        val response = searchPaymentHistoryUseCase.searchHttpPaymentDetailHistory(userId)
        return ApiResult(
            data = response,
            status = HttpStatus.OK.value(),
            message = "유저 결제 내역 조회 성공"
        )
    }

    @GetMapping("/api/v2/user/{userId}/payment-detail-history")
    suspend fun searchHttpPaymentDetailHistory(@PathVariable userId: Long): ApiResult<SearchPaymentHistoryResponse> {
        val response = searchPaymentHistoryUseCase.searchGrpcPaymentDetailHistory(userId)
        return ApiResult(
            data = response,
            status = HttpStatus.OK.value(),
            message = "유저 결제 내역 조회 성공"
        )
    }
}