package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.application.service.command.ReserveSeatCommand
import org.ktor_lecture.concertservice.application.service.dto.ReserveSeatInfo

interface ReserveSeatUseCase {
    fun reserveSeat(command: ReserveSeatCommand): ReserveSeatInfo
}