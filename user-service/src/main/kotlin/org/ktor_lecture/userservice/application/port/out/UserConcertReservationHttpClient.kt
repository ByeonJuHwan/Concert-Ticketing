package org.ktor_lecture.userservice.application.port.out

import org.ktor_lecture.userservice.adapter.out.api.grpc.response.SearchConcertsResponse
import org.ktor_lecture.userservice.adapter.out.api.grpc.response.SearchReservationResponse

interface UserConcertReservationHttpClient {
    fun searchConcerts(userId: Long): List<SearchConcertsResponse>
    fun searchReservations(userId: Long): List<SearchReservationResponse>
}