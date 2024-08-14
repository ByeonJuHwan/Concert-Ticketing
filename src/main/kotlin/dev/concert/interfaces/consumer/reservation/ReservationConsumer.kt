package dev.concert.interfaces.consumer.reservation

import dev.concert.application.data.DataPlatformFacade
import dev.concert.application.reservation.ReservationFacade
import dev.concert.domain.util.message.MessageManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class ReservationConsumer (
    private val dataPlatformFacade: DataPlatformFacade,
    private val reservationFacade: ReservationFacade,
    private val messageManager: MessageManager,
) {

    private val log : Logger = LoggerFactory.getLogger(ReservationConsumer::class.java)

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
            // TODO 외부 API 처리에 reservation outbox 처리를 함께 넣는건 강한 커플링이므로 제거 대신 AFTER_COMMIT 에서 상태변경
//            reservationFacade.changeReservationOutBoxStatusSendSuccess(reservationId.toLong())
            dataPlatformFacade.sendReservationData(reservationId.toLong())
        }.onFailure { ex ->
            // 아웃박스 패턴으로 발행은 정상적으로 이루어지는게 보장됨
            // 비즈니스로직 혹은 외부 api 문제일 수 있으므로 예외를 슬랙으로 보내도록 처리
            log.error("데이터 플랫폼 전송 에러 : ${ex.message}", ex)
            messageManager.sendMessage("예약 데이터 전송 수신 에러 -> ${ex.message}")
        }
    }
}