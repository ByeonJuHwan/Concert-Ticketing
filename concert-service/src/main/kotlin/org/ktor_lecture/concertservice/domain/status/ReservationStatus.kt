package org.ktor_lecture.concertservice.domain.status

enum class ReservationStatus {
    PENDING, // 예약이 임시로 저장된 상태
    EXPIRED, // 임시 예약이 만료된 상태
    PAID, // 결제가 완료된 상태
}