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

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @KafkaListener(topics = ["reservation"], groupId = "concert_group")
    fun handleExternalApiKafkaEvent(reservationId : String) {
        log.info("아웃박스 리스터로부터 수신 성공!!")
        log.info("Kafka Event 수신 성공!!")
        runCatching {
            reservationFacade.changeReservationOutBoxStatusSendSuccess(reservationId.toLong())
            dataPlatformFacade.sendReservationData(reservationId.toLong())
        }.onFailure { ex ->
            // 예외 처리 로직
            log.error("데이터 플랫폼 전송 에러 : ${ex.message}", ex)
            // TODO 재시도 로직도 추가해야함
            reservationFacade.changeReservationOutBoxStatusSendFail(reservationId.toLong())
        }
    }
}