package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.application.service.command.ChangeSeatStatusReservedCommand

interface ChangeSeatReservedUseCase {
    fun changeSeatStatusReserved(command: ChangeSeatStatusReservedCommand)

}