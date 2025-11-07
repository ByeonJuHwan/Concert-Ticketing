package org.ktor_lecture.concertservice.application.service.dto

import org.ktor_lecture.concertservice.domain.entity.ConcertOptionEntity

data class ConcertDateInfo(
    val concertId : Long,
    val concertName : String,
    val concertDate : String,
    val concertTime : String,
    val concertVenue : String,
    val availableSeats : Int,
) {
    companion object {
        fun from(concertOptionEntity: ConcertOptionEntity): ConcertDateInfo {
            return ConcertDateInfo(
                concertId = concertOptionEntity.concert.id!!,
                concertName = concertOptionEntity.concert.concertName,
                concertDate = concertOptionEntity.concertDate,
                concertTime = concertOptionEntity.concertTime,
                concertVenue = concertOptionEntity.concertVenue,
                availableSeats = concertOptionEntity.availableSeats,
            )
        }
    }
}
