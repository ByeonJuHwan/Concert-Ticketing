package dev.concert.interfaces.event

import dev.concert.application.data.DataPlatformFacade
import dev.concert.application.reservation.ReservationFacade
import dev.concert.domain.event.reservation.ReservationSuccessEvent
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

    @Async
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleExternalApiEvent(event: ReservationSuccessEvent) {
        runCatching {
            dataPlatformFacade.sendReservationData(event.reservationId)
        }.onFailure { ex ->
            // 예외 처리 로직
            log.error("데이터 플랫폼 전송 에러 : ${ex.message}", ex)
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun handleReservationOutBox(event: ReservationSuccessEvent) {
        log.info("BEFORE_COMMIT : 아웃박스 이벤트 발행")
        reservationFacade.recordReservationOutBoxMsg(event)
    }

    /**
     * 아웃 박스 패턴 적용
     *
     * 성공시 -> SEND_SUCCESS 로 상태변경
     * 실패시 -> SEND_FAIL 로 상태 변경
     *
     * 이후 SEND_FAIL, INIT 인 상태인 이벤트들을 재시도 처리
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @KafkaListener(topics = ["reservation"], groupId = "concert_group")
    fun handleExternalApiKafkaEvent(reservationId : String) {
        log.info("Kafka Event 수신 성공!!")
        runCatching {
            reservationFacade.changeReservationOutBoxStatusSendSuccess(reservationId.toLong())
            dataPlatformFacade.sendReservationData(reservationId.toLong())
        }.onFailure { ex ->
            // 예외 처리 로직
            log.error("데이터 플랫폼 전송 에러 : ${ex.message}", ex)
            reservationFacade.changeReservationOutBoxStatusSendFail(reservationId.toLong())
        }
    }
}