package org.ktor_lecture.userservice.adapter.out.persistence

import org.ktor_lecture.userservice.adapter.out.persistence.jpa.OutBoxJpaRepository
import org.ktor_lecture.userservice.application.port.out.OutBoxRepository
import org.ktor_lecture.userservice.domain.entity.OutBox
import org.ktor_lecture.userservice.domain.entity.OutboxStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.time.LocalDateTime

@Component
class OutBoxRepositoryAdapter(
    private val outBoxJpaRepository: OutBoxJpaRepository,
) : OutBoxRepository {

    private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)

    override fun save(outBox: OutBox) {
        outBoxJpaRepository.save(outBox)
    }

    @Transactional
    override fun updateStatus(eventId: String, status: OutboxStatus) {
        log.info("=== 비즈니스 로직 실행 ===")
        log.info("트랜잭션 활성: {}", TransactionSynchronizationManager.isActualTransactionActive())
        outBoxJpaRepository.updateStatus(eventId, status)
    }

    override fun getFailedEvents(): List<OutBox> {
        return outBoxJpaRepository.getFailedEvents(listOf(OutboxStatus.FAILED, OutboxStatus.PENDING), LocalDateTime.now().minusMinutes(10))
    }

    override fun increaseRetryCount(eventId: String) {
        outBoxJpaRepository.incrementRetryCount(eventId)
    }
}