package org.ktor_lecture.userservice.adapter.out.persistence.jpa

import org.ktor_lecture.userservice.domain.entity.OutBox
import org.ktor_lecture.userservice.domain.entity.OutboxStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime


interface OutBoxJpaRepository: JpaRepository<OutBox, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OutBox o SET o.status = :status WHERE o.eventId = :eventId")
    fun updateStatus(eventId: String, status: OutboxStatus)

    @Query("SELECT o FROM OutBox o WHERE o.status IN :statuses AND o.createdAt < :tenMinutesAgo")
    fun getFailedEvents(@Param("statuses") statuses: List<OutboxStatus>, @Param("tenMinutesAgo") tenMinutesAgo: LocalDateTime): List<OutBox>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OutBox o SET o.retryCount = o.retryCount + 1 WHERE o.eventId = :eventId")
    fun incrementRetryCount(eventId: String)
}