package org.ktor_lecture.paymentservice.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

const val MAX_RETRY = 3

@Entity
@Table(name = "saga")
class SagaEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 50)
    val sagaType: String, // PAYMENT, USER 등

    @Column(length = 50)
    var currentStep: String? = null,

    @Column(columnDefinition = "TEXT")
    var completedSteps: String = "", // JSON Array로 저장: ["POINT_DEDUCT", "RESERVATION_CONFIRM"]

    @Column(length = 50)
    var failedStep: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: SagaStatus = SagaStatus.IN_PROGRESS,

    @Column(name = "payload", columnDefinition = "TEXT")
    var payload: String? = null,

    var retryCount: Int = 0,

    @Column
    var completedAt: LocalDateTime? = null
): BaseEntity() {

    fun addCompletedStep(step: String) {
        completedSteps = if (completedSteps.isEmpty()) {
            step
        } else {
            "$completedSteps,$step"
        }
    }

    fun getCompletedStepList(): List<String> {
        return if (completedSteps.isEmpty()) {
            emptyList()
        } else {
            completedSteps.split(",")
        }
    }

    fun failed(stepName: String) {
        failedStep = stepName
        status = SagaStatus.FAILED
    }

    fun complete() {
        status = SagaStatus.COMPLETED
        completedAt = LocalDateTime.now()
    }

    fun compensating(payload: String) {
        status = SagaStatus.COMPENSATING
        this.payload = payload
    }

    fun compensated() {
        status = SagaStatus.COMPENSATED
        completedAt = LocalDateTime.now()
    }

    fun increaseRetryCount() {
        retryCount += 1
    }

    fun isRetryAvailable(): Boolean {
        return retryCount < MAX_RETRY
    }

    fun alert() {
        status = SagaStatus.ALERT
    }
}

enum class SagaStatus {
    IN_PROGRESS,    // 진행 중
    COMPLETED,      // 완료
    FAILED,         // 실패
    COMPENSATING,   // 보상 중
    COMPENSATED,    // 보상 완료
    ALERT,          // 수동처리 알림 발송
}