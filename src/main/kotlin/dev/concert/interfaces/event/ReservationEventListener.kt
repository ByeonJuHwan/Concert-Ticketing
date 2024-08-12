package dev.concert.interfaces.event

import dev.concert.application.data.DataPlatformFacade
import dev.concert.application.reservation.ReservationFacade
import dev.concert.domain.event.reservation.ReservationEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ReservationEventListener (
    private val dataPlatformFacade: DataPlatformFacade,
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

    /**
     * 아웃 박스 패턴 적용
     *
     * 1. 아웃박스 상태를 SEND_SUCCESS 로 상태변경
     * 2. 예약 관련 외부 API 호출 (이로직에서는 Slack)
     */
    @Async
    @KafkaListener(topics = ["reservation"], groupId = "concert_group")
    fun handleExternalApiKafkaEvent(reservationId : String) {
        log.info("Kafka Event 수신 성공!!")
        runCatching {
            reservationFacade.changeReservationOutBoxStatusSendSuccess(reservationId.toLong())
            dataPlatformFacade.sendReservationData(reservationId.toLong())
        }.onFailure { ex ->
            // 아웃박스 패턴으로 발행은 정상적으로 이루어지는게 보장됨
            // 비즈니스로직 혹은 외부 api 문제일 수 있으므로 예외를 슬랙으로 보내도록 처리
            log.error("데이터 플랫폼 전송 에러 : ${ex.message}", ex)
        }
    }
}