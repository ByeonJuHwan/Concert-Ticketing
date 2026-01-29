package org.ktor_lecture.userservice.adapter.out.api.grpc.response

data class SearchConcertsResponse(
    val concertId: Long,
    val concertName: String,
    val concertStartDate: String,
    val concertEndDate: String,
)
