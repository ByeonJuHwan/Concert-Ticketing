package dev.concert.presentation.response.concert

import dev.concert.domain.entity.ConcertEntity

data class ConcertResponse(
    val concerts: List<ConcertEntity>,
)