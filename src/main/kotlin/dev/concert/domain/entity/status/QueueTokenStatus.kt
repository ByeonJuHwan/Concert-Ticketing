package dev.concert.domain.entity.status

enum class QueueTokenStatus {
    WAITING, // 대기중
    ACTIVE, // 활성화
    EXPIRED, // 만료됨
}