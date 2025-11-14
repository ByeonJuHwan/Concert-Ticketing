package org.ktor_lecture.userservice.application.port.out

import org.ktor_lecture.userservice.domain.entity.OutBox
import org.ktor_lecture.userservice.domain.entity.OutboxStatus

interface OutBoxRepository {
    fun save(outBox: OutBox)
    fun updateStatus(eventId: String, status: OutboxStatus)
    fun getFailedEvents(): List<OutBox>
    fun increaseRetryCount(eventId: String)
}