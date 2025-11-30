package org.ktor_lecture.concertservice.adapter.`in`.batch

import org.ktor_lecture.concertservice.application.port.`in`.SeatReservationAvailableUseCase
import org.ktor_lecture.concertservice.common.DistributedLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SeatReservationScheduler (
    private val seatReservationAvailableUseCase: SeatReservationAvailableUseCase,
) {
    /**
     * 예약 만료 시간을 관리하는 스케줄러
     */
    @DistributedLock(
        key = "seat:reservation:batch"
    )
    @Scheduled(fixedRate = 60000)
    fun seatReservationScheduler() {
        seatReservationAvailableUseCase.seatReservationAvailable()
    }
}