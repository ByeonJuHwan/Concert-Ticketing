package org.ktor_lecture.concertservice.application.service

import org.ktor_lecture.concertservice.application.port.`in`.ReservationCreatedOutBoxUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SendReservationCreatedUseCase
import org.ktor_lecture.concertservice.application.port.out.EventPublisher
import org.ktor_lecture.concertservice.application.port.out.OutBoxRepository
import org.ktor_lecture.concertservice.common.JsonUtil
import org.ktor_lecture.concertservice.domain.entity.OutBox
import org.ktor_lecture.concertservice.domain.event.ReservationCreatedEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReservationEventService (
    private val outBoxRepository: OutBoxRepository,
    @Qualifier("kafka") private val eventPublisher: EventPublisher,
) : ReservationCreatedOutBoxUseCase, SendReservationCreatedUseCase {

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
}