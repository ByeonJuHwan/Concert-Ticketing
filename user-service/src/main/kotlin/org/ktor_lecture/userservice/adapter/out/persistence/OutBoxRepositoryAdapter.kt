package org.ktor_lecture.userservice.adapter.out.persistence

import org.ktor_lecture.userservice.adapter.out.persistence.jpa.OutBoxJpaRepository
import org.ktor_lecture.userservice.application.port.out.OutBoxRepository
import org.ktor_lecture.userservice.domain.entity.OutBox
import org.ktor_lecture.userservice.domain.entity.OutboxStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class OutBoxRepositoryAdapter(
    private val outBoxJpaRepository: OutBoxJpaRepository,
) : OutBoxRepository {

    override fun save(outBox: OutBox) {
        outBoxJpaRepository.save(outBox)
    }

    @Transactional
    override fun updateStatus(eventId: String, status: OutboxStatus) {
        outBoxJpaRepository.updateStatus(eventId, status)
    }
}