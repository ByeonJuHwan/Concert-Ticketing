package org.ktor_lecture.userservice.adapter.out.persistence.jpa

import org.ktor_lecture.userservice.domain.entity.OutBox
import org.ktor_lecture.userservice.domain.entity.OutboxStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query


interface OutBoxJpaRepository: JpaRepository<OutBox, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OutBox o SET o.status = :status WHERE o.eventId = :eventId")
    fun updateStatus(eventId: String, status: OutboxStatus)
}