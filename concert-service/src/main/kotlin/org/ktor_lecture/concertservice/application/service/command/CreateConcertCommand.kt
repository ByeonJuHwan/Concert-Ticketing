package org.ktor_lecture.concertservice.application.service.command

import java.time.LocalDate

data class CreateConcertCommand(
    val concertName: String,
    val singer: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val reserveStartDate: LocalDate,
    val reserveEndDate: LocalDate,
)
