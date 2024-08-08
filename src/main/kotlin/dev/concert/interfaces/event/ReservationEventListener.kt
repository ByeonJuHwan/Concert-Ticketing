package dev.concert.interfaces.event

import dev.concert.application.data.DataPlatformFacade
import dev.concert.domain.service.reservation.event.ReservationSuccessEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ReservationEventListener (
    private val dataPlatformFacade: DataPlatformFacade,
) {

    private val log : Logger = LoggerFactory.getLogger(ReservationEventListener::class.java)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleExternalApiEvent(event: ReservationSuccessEvent) {
        runCatching {
            dataPlatformFacade.sendReservationData(event.reservationId)
        }.onFailure { ex ->
            // 예외 처리 로직
            log.error("데이터 플랫폼 전송 에러 : ${ex.message}", ex)
        }
    }
}