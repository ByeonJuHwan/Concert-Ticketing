package org.ktor_lecture.userservice.application.port.out

import org.ktor_lecture.userservice.adapter.out.api.grpc.response.SearchConcertsResponse
import org.ktor_lecture.userservice.adapter.out.api.grpc.response.SearchReservationResponse

interface UserConcertReservationGrpcClient {
    suspend fun searchConcerts(userId: Long): List<SearchConcertsResponse>
    suspend fun searchReservations(userId: Long): List<SearchReservationResponse>
}