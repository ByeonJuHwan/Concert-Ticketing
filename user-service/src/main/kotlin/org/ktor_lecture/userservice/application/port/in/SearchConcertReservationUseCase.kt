package org.ktor_lecture.userservice.application.port.`in`

import org.ktor_lecture.userservice.adapter.`in`.web.response.SearchConcertReservationResponse

interface SearchConcertReservationUseCase {
    suspend fun searchGrpcConcertReservationHistory(userId: Long): SearchConcertReservationResponse
    fun searchHttpConcertReservationHistory(userId: Long): SearchConcertReservationResponse
}