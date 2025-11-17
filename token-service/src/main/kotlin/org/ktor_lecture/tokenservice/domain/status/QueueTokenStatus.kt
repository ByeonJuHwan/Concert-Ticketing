package org.ktor_lecture.tokenservice.domain.status

enum class QueueTokenStatus {
    WAITING, // 대기중
    ACTIVE, // 활성화
    EXPIRED, // 만료됨
}