package org.ktor_lecture.userservice.adapter.`in`.web.api

import org.ktor_lecture.userservice.adapter.`in`.web.ApiResult
import org.ktor_lecture.userservice.adapter.`in`.web.request.CreateUserRequest
import org.ktor_lecture.userservice.adapter.`in`.web.request.toCommand
import org.ktor_lecture.userservice.adapter.`in`.web.response.SearchConcertReservationResponse
import org.ktor_lecture.userservice.application.port.`in`.CreateUserUseCase
import org.ktor_lecture.userservice.application.port.`in`.SearchConcertReservationUseCase
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController (
    private val createUserUseCase: CreateUserUseCase,
    private val searchConcertReservationUseCase: SearchConcertReservationUseCase,
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

    @GetMapping("/api/v1/user/{userId}/http")
    fun searchGrpcPaymentDetailHistory(@PathVariable userId: Long): ApiResult<SearchConcertReservationResponse> {
        val response = searchConcertReservationUseCase.searchHttpConcertReservationHistory(userId)
        return ApiResult(
            data = response,
            status = HttpStatus.OK.value(),
            message = "유저 결제 내역 조회 성공"
        )
    }

    @GetMapping("/api/v2/user/{userId}/grpc")
    suspend fun searchHttpPaymentDetailHistory(@PathVariable userId: Long): ApiResult<SearchConcertReservationResponse> {
        val response = searchConcertReservationUseCase.searchGrpcConcertReservationHistory(userId)
        return ApiResult(
            data = response,
            status = HttpStatus.OK.value(),
            message = "유저 결제 내역 조회 성공"
        )
    }
}