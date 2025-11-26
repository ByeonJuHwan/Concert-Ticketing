package org.ktor_lecture.userservice.adapter.`in`.web.api

import org.ktor_lecture.userservice.adapter.`in`.web.ApiResult
import org.ktor_lecture.userservice.adapter.`in`.web.request.CreateUserRequest
import org.ktor_lecture.userservice.adapter.`in`.web.request.toCommand
import org.ktor_lecture.userservice.application.port.`in`.CreateUserUseCase
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController (
    private val createUserUseCase: CreateUserUseCase,
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
}