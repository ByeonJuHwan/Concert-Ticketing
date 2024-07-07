package dev.concert.application.token.dto

import dev.concert.domain.entity.status.QueueTokenStatus

data class TokenResponseDto(
    val token : String,
    val status : QueueTokenStatus,
    val queueOrder : Int,
    val remainingTime : Long,
)
