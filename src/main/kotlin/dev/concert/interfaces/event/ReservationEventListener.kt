package dev.concert.interfaces.event

import dev.concert.application.reservation.ReservationFacade
import dev.concert.domain.event.reservation.ReservationEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ReservationEventListener (
    private val reservationFacade: ReservationFacade,
) {

    private val log : Logger = LoggerFactory.getLogger(ReservationEventListener::class.java)

    /*
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleExternalApiEvent(event: ReservationEvent) {
        runCatching {
            dataPlatformFacade.sendReservationData(event.toEntity().reservationId)
        }.onFailure { ex ->
            // 예외 처리 로직
            log.error("데이터 플랫폼 전송 에러 : ${ex.message}", ex)
        }
    }
    */

    /**
     * [아웃박스 패턴]
     * BEFORE_COMMIT 으로 실행되어 어떤 이벤트가 발행되어야 하는지 저장한다
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun handleReservationOutBox(event: ReservationEvent) {
        log.info("BEFORE_COMMIT : 아웃박스 이벤트 발행")
        reservationFacade.recordReservationOutBoxMsg(event)
    }

    /**
     * [아웃박스 패턴]
     * AFTER_COMMIT 시 카프카 이벤트를 발행한다
     * 만약 카프카 이벤트를 발행했지만 발행과정에서 예외발생시 SEND_FAIL 상태로 변경된다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun publishReservationEvent(event: ReservationEvent) {
        // 카프카 이벤트 발행
        runCatching {
            reservationFacade.publishReservationEvent(event)
        }.onFailure { e ->
            log.error("Kafka Message Send Failed!" , e)
            reservationFacade.changeReservationOutBoxStatusSendFail(event.toEntity().reservationId)
        }
    }
}