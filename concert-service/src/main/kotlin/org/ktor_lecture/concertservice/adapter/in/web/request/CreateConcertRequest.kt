package org.ktor_lecture.concertservice.adapter.`in`.web.request

import org.ktor_lecture.concertservice.application.service.command.CreateConcertCommand
import java.time.LocalDate

data class CreateConcertRequest(
    val concertName: String,
    val singer: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val reserveStartDate: LocalDate,
    val reserveEndDate: LocalDate,
) {
    fun toCommand() = CreateConcertCommand(concertName, singer, startDate, endDate, reserveStartDate, reserveEndDate)
}
