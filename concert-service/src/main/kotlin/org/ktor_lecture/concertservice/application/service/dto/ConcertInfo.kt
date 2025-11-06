package org.ktor_lecture.concertservice.application.service.dto

data class ConcertInfo (
    val id : Long,
    val concertName : String,
    val singer : String,
    val startDate : String,
    val endDate : String,
    val reserveStartDate : String,
    val reserveEndDate : String,
)