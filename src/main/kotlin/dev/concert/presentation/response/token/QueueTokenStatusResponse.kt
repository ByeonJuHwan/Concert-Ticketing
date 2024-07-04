package dev.concert.presentation.response.token

import dev.concert.infrastructure.entity.status.QueueTokenStatus

data class QueueTokenStatusResponse(
    val token: String,
    val status: QueueTokenStatus,
    val queueOrder: Int,
    val remainingTime: Long,
)
