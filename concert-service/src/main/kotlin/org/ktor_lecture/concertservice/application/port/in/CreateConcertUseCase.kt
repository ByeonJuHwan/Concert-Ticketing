package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.application.service.command.CreateConcertCommand

interface CreateConcertUseCase {
    fun createConcert(command: CreateConcertCommand)

}