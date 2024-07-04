package dev.concert.presentation.response.concert

import dev.concert.infrastructure.entity.ConcertEntity

data class ConcertResponse(
    val concerts: List<ConcertEntity>,
)