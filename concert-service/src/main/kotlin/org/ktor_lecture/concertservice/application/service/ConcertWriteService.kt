package org.ktor_lecture.concertservice.application.service

import org.ktor_lecture.concertservice.application.port.`in`.ReserveSeatUseCase
import org.ktor_lecture.concertservice.application.port.out.ConcertWriteRepository
import org.ktor_lecture.concertservice.application.service.command.ReserveSeatCommand
import org.ktor_lecture.concertservice.application.service.dto.ReserveSeatInfo
import org.springframework.stereotype.Service

@Service
class ConcertWriteService (
    private val concertWriteRepository: ConcertWriteRepository,
): ReserveSeatUseCase {
    override fun reserveSeat(command: ReserveSeatCommand): ReserveSeatInfo {
        TODO("Not yet implemented")
    }
}