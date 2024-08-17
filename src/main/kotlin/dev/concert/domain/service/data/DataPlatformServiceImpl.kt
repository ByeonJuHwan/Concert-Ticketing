package dev.concert.domain.service.data

import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.util.message.MessageManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DataPlatformServiceImpl (
    private val messageManager: MessageManager
) : DataPlatformService {

    private val log : Logger = LoggerFactory.getLogger(DataPlatformServiceImpl::class.java)

    override fun sendReservationData(reservation: ReservationEntity) {
        messageManager.sendMessage("예약 데이터 전송 reservationId : ${reservation.id}")
        log.info("외부 API 통신 성공")
    }
}