package dev.concert.application.concert.dto

data class ConcertsDto(
    val id : Long,
    val concertName : String,
    val singer : String,
    val startDate : String,
    val endDate : String,
    val reserveStartDate : String,
    val reserveEndDate : String,
 )
