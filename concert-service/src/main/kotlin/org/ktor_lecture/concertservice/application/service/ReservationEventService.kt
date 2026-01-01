package org.ktor_lecture.concertservice.application.service

import kotlinx.serialization.SerializationException
import org.ktor_lecture.concertservice.application.port.`in`.ReservationCreatedEventRetryUseCase
import org.ktor_lecture.concertservice.application.port.`in`.ReservationCreatedOutBoxUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SendReservationCreatedUseCase
import org.ktor_lecture.concertservice.application.port.out.EventPublisher
import org.ktor_lecture.concertservice.application.port.out.OutBoxRepository
import org.ktor_lecture.concertservice.common.JsonUtil
import org.ktor_lecture.concertservice.domain.entity.OutBox
import org.ktor_lecture.concertservice.domain.entity.OutboxStatus
import org.ktor_lecture.concertservice.domain.event.ReservationCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReservationEventService (
    private val outBoxRepository: OutBoxRepository,
    @Qualifier("kafka") private val eventPublisher: EventPublisher,
) : ReservationCreatedOutBoxUseCase, SendReservationCreatedUseCase, ReservationCreatedEventRetryUseCase {

    private val log = LoggerFactory.getLogger(this::class.java)

    /**
     * 좌석 예약 이벤트 아웃박스 메시지 저장
     */
    @Transactional
    override fun handleReservationCreatedOutBox(event: ReservationCreatedEvent) {
        val outbox = OutBox(
            eventId = event.eventId,
            aggregateType = "ReservationEntity",
            aggregateId = event.reservationId.toString(),
            eventType = "ReservationCreatedEvent",
            payload = JsonUtil.encodeToJson(event),
        )

        outBoxRepository.save(outbox)
    }

    /**
     * 좌석 예약 성공 이벤트 발행
     */
    override fun publishReservationCreatedEvent(event: ReservationCreatedEvent) {
        eventPublisher.publish("reservation.create", event)
    }


    /**
     * 발행이 실패한 이벤트들을 다시 재시도 한다
     *
     * 1. 이벤트 상태가 PENDING, FAILED 이면서 created_at이 10분 지난 예약 이벤트 조회
     * 2. 재시도 횟수 체크
     * 3. 이벤트 재발송
     */
    @Transactional
    override fun retryReservationCreatedEvent() {
        val outboxes = outBoxRepository.getFailedEvents()

        if(outboxes.isEmpty()) {
            log.info("재시도할 이벤트가 존재하지 않습니다.")
            return
        }

        outboxes.forEach { outBox ->
            try {
                if (outBox.retryCount >= outBox.maxRetryCount) {
                    log.warn("최대 재시도 횟수 초가: eventId = {}", outBox.eventId)
                    outBoxRepository.updateStatus(outBox.eventId, OutboxStatus.CANT_RETRY)
                    return
                }

                val reservationCreatedEvent = JsonUtil.decodeFromJson<ReservationCreatedEvent>(outBox.payload)

                outBoxRepository.increaseRetryCount(outBox.eventId)

                eventPublisher.publish("reservation.create",reservationCreatedEvent)
            } catch (e: SerializationException) {
                // 직렬화 에러
                log.error("Event 직렬화 에러 : {}", outBox.eventId, e)
                outBoxRepository.updateStatus(outBox.eventId, OutboxStatus.CANT_RETRY)
            } catch (e: IllegalArgumentException) {
                // 유효하지 않은 요청값
                log.error("유효하지 않은 이벤트 요청값 error : {}", outBox.eventId, e)
                outBoxRepository.updateStatus(outBox.eventId, OutboxStatus.CANT_RETRY)
            } catch (e: Exception) {
                log.error("재시도 에러 발생 : {}", outBox.eventId, e)
                outBoxRepository.increaseRetryCount(outBox.eventId)
            }
        }
    }
}