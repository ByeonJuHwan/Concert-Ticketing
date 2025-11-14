package org.ktor_lecture.concertservice.application.port.out

import org.ktor_lecture.concertservice.domain.entity.OutBox
import org.ktor_lecture.concertservice.domain.entity.OutboxStatus


interface OutBoxRepository {
    fun save(outBox: OutBox)
    fun updateStatus(eventId: String, status: OutboxStatus)
    fun getFailedEvents(): List<OutBox>
    fun increaseRetryCount(eventId: String)
}