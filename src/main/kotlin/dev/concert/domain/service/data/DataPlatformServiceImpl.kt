package dev.concert.domain.service.data

import dev.concert.domain.entity.ReservationEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DataPlatformServiceImpl (

) : DataPlatformService {

    private val log : Logger = LoggerFactory.getLogger(DataPlatformServiceImpl::class.java)

    override fun sendReservationData(reservation: ReservationEntity) {
        log.info("외부 API 통신 성공")
        // TODO 슬랙, 텔레그램 알림으로 변경해보기
    }
}