package org.ktor_lecture.concertservice.application.service.dto

import org.ktor_lecture.concertservice.domain.entity.ConcertEntity

data class ConcertInfo (
    val id : Long,
    val concertName : String,
    val singer : String,
    val startDate : String,
    val endDate : String,
    val reserveStartDate : String,
    val reserveEndDate : String,
) {
    companion object {
        fun from(concert: ConcertEntity): ConcertInfo {
            return ConcertInfo(
                id = concert.id!!,
                concertName = concert.concertName,
                singer = concert.singer,
                startDate = concert.startDate.toString(),
                endDate = concert.endDate.toString(),
                reserveStartDate = concert.reserveStartDate.toString(),
                reserveEndDate = concert.reserveEndDate.toString(),
            )
        }
    }
}