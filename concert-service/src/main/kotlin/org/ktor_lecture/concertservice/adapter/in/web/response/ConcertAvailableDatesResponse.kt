package org.ktor_lecture.concertservice.adapter.`in`.web.response

import org.ktor_lecture.concertservice.application.service.dto.ConcertDateInfo


data class ConcertAvailableDatesResponse(
    val concertId : Long,
    val concertName : String,
    val concertDate : String,
    val concertTime : String,
    val concertVenue : String,
    val availableSeats : Int,
) {
    companion object {
        fun from(info: ConcertDateInfo): ConcertAvailableDatesResponse {
            return ConcertAvailableDatesResponse(
                concertId = info.concertId,
                concertName = info.concertName,
                concertDate = info.concertDate,
                concertTime = info.concertTime,
                concertVenue = info.concertVenue,
                availableSeats = info.availableSeats,
            )
        }

        fun fromList(infos: List<ConcertDateInfo>): List<ConcertAvailableDatesResponse> {
            return infos.map { from(it) }
        }
    }
}
