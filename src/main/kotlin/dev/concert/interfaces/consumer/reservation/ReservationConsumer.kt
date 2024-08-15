package dev.concert.interfaces.consumer.reservation

import dev.concert.application.data.DataPlatformFacade
import dev.concert.domain.util.message.MessageManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class ReservationConsumer (
    private val dataPlatformFacade: DataPlatformFacade,
    private val messageManager: MessageManager,
) {

    private val log : Logger = LoggerFactory.getLogger(ReservationConsumer::class.java)

    /**
     * [아웃 박스 패턴]
     *
     * 1. 예약 관련 외부 API 호출 (이로직에서는 Slack)
     * 2. 아웃박스 패턴으로 이벤트 발행이 보장됨으로 실패시 개발자가 알수 있도록 처리
     */
    @Async
    @KafkaListener(topics = ["reservation"], groupId = "concert_group")
    fun handleExternalApiKafkaEvent(reservationId : String) {
        log.info("Kafka Event 수신 성공!!")
        runCatching {
            dataPlatformFacade.sendReservationData(reservationId.toLong())
        }.onFailure { ex ->
            // 아웃박스 패턴으로 발행은 정상적으로 이루어지는게 보장됨
            // 비즈니스로직 혹은 외부 api 문제일 수 있으므로 예외를 슬랙으로 보내도록 처리
            log.error("데이터 플랫폼 전송 에러 : ${ex.message}", ex)
            messageManager.sendMessage("예약 데이터 전송 수신 에러 -> ${ex.message}")
        }
    }
}