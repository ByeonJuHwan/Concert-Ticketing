package dev.concert.presentation.response.concert

import dev.concert.application.concert.dto.ConcertDatesDto

data class ConcertAvailableDatesResponse(
    val concerts: List<ConcertDatesDto>,
)
