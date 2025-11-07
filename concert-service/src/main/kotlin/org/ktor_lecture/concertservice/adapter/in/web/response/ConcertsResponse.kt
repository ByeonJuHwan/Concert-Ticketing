package org.ktor_lecture.concertservice.adapter.`in`.web.response

import org.ktor_lecture.concertservice.application.service.dto.ConcertInfo


data class ConcertsResponse(
    val id: Long,
    val concertName: String,
    val singer: String,
    val startDate: String,
    val endDate: String,
    val reserveStartDate: String,
    val reserveEndDate: String,
) {
    companion object {
        fun from(info: ConcertInfo): ConcertsResponse {
            return ConcertsResponse(
                id = info.id,
                concertName = info.concertName,
                singer = info.singer,
                startDate = info.startDate,
                endDate = info.endDate,
                reserveStartDate = info.reserveStartDate,
                reserveEndDate = info.reserveEndDate,
            )
        }

        fun fromList(infos: List<ConcertInfo>): List<ConcertsResponse> {
            return infos.map { from(it) }
        }
    }
}