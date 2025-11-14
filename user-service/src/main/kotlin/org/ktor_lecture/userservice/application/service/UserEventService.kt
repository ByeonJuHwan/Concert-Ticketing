package org.ktor_lecture.userservice.application.service

import kotlinx.serialization.SerializationException
import org.ktor_lecture.userservice.application.port.`in`.CreateUserCreateOutBoxUseCase
import org.ktor_lecture.userservice.application.port.`in`.SendUserCreatedEventUseCase
import org.ktor_lecture.userservice.application.port.`in`.UserCreatedEventRetryUseCase
import org.ktor_lecture.userservice.application.port.out.EventPublisher
import org.ktor_lecture.userservice.application.port.out.OutBoxRepository
import org.ktor_lecture.userservice.common.JsonUtil
import org.ktor_lecture.userservice.domain.entity.OutBox
import org.ktor_lecture.userservice.domain.entity.OutboxStatus
import org.ktor_lecture.userservice.domain.event.UserCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserEventService(
    private val txAdvice: TxAdvice,
    private val outBoxRepository: OutBoxRepository,
    @Qualifier("kafka") private val eventPublisher: EventPublisher,
): CreateUserCreateOutBoxUseCase, SendUserCreatedEventUseCase, UserCreatedEventRetryUseCase {

    private val log = LoggerFactory.getLogger(this::class.java)

    /**
     * 아웃박스 메세지 생성
     */
    @Transactional
    override fun recordUserCreatedOutBoxMsg(event: UserCreatedEvent) {
        val outBox = OutBox(
            aggregateType = "UserEntity",
            eventId = event.eventId,
            aggregateId = event.userId,
            eventType = "UserCreatedEvent",
            payload = JsonUtil.encodeToJson(event)
        )

        outBoxRepository.save(outBox)
    }


    /**
     * 이벤트 발행
     */
    override fun publishUseCreatedEvent(event: UserCreatedEvent) {
        eventPublisher.publish(event)
    }

    /**
     * 발행이 실패한 이벤트들을 다시 재시도 한다
     *
     * 1. 이벤트 상태가 PENDING, FAILED 이면서 created_at이 10분 지난 예약 이벤트 조회
     * 2. 재시도 횟수 체크
     * 3. 이벤트 재발송
     */
    override fun userCreatedEventRetryScheduler() {
        val outboxes = outBoxRepository.getFailedEvents()

        if(outboxes.isEmpty()) {
            log.info("재시도할 이벤트가 존재하지 않습니다.")
            return
        }

        outboxes.forEach { outBox ->
            txAdvice.run {
                try {
                    if (outBox.retryCount >= outBox.maxRetryCount) {
                        log.warn("최대 재시도 횟수 초가: eventId = {}", outBox.eventId)
                        outBoxRepository.updateStatus(outBox.eventId, OutboxStatus.CANT_RETRY)
                        return@run
                    }

                    val userCreatedEvent = JsonUtil.decodeFromJson<UserCreatedEvent>(outBox.payload)

                    outBoxRepository.increaseRetryCount(outBox.eventId)

                    eventPublisher.publish(userCreatedEvent)
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
}
