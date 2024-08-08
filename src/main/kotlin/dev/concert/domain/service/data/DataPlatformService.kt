package dev.concert.domain.service.data

import dev.concert.domain.entity.ReservationEntity

interface DataPlatformService {
    fun sendReservationData(reservation : ReservationEntity)
}