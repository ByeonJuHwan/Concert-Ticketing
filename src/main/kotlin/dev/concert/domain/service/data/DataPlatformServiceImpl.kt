package dev.concert.domain.service.data

import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.service.data.message.MessageManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DataPlatformServiceImpl (
    private val messageManager: MessageManager
) : DataPlatformService {

    private val log : Logger = LoggerFactory.getLogger(DataPlatformServiceImpl::class.java)

    override fun sendReservationData(reservation: ReservationEntity) {
        messageManager.sendMessage()
        log.info("외부 API 통신 성공")
    }
}