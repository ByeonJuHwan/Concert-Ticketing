package org.ktor_lecture.concertservice.adapter.`in`.event

import org.ktor_lecture.concertservice.application.port.`in`.ReservationCreatedOutBoxUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SendReservationCreatedUseCase
import org.ktor_lecture.concertservice.domain.event.ReservationCreatedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ReservationEventListener(
    private val reservationCreatedOutBoxUseCase: ReservationCreatedOutBoxUseCase,
    private val sendReservationCreatedUseCase: SendReservationCreatedUseCase,
) {

    /**
     * [아웃박스 패턴]
     * BEFORE_COMMIT 으로 실행되어 어떤 이벤트가 발행되어야 하는지 저장한다
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun handleReservationCreatedOutBox(event: ReservationCreatedEvent) {
        reservationCreatedOutBoxUseCase.handleReservationCreatedOutBox(event)
    }

    /**
     * [아웃박스 패턴]
     * AFTER_COMMIT 시 카프카 이벤트를 발행한다
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun publishReservationCreatedEvent(event: ReservationCreatedEvent) {
        sendReservationCreatedUseCase.publishReservationCreatedEvent(event)
    }
}