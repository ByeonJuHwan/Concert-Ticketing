package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.application.service.command.ChangeConcertOptionCommand

interface ChangeConcertOptionUseCase {
    fun changeConcertOption(concertId: Long, concertOptionId: Long, command: ChangeConcertOptionCommand)
}