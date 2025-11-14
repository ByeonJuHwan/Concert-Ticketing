package org.ktor_lecture.userservice.application.port.`in`.point

import org.ktor_lecture.userservice.adapter.`in`.web.response.CurrentPointResponse

interface SearchCurrentPointsUseCase {
    fun getCurrentPoint(userId: Long): CurrentPointResponse
}