package dev.concert.interfaces.event

import dev.concert.application.data.DataPlatformFacade
import dev.concert.domain.service.reservation.event.ReservationSuccessEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ReservationEventListener (
    private val dataPlatformFacade: DataPlatformFacade,
) {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleExternalApiEvent(event: ReservationSuccessEvent) {
        dataPlatformFacade.sendReservationData(event.reservationId)
    }
}