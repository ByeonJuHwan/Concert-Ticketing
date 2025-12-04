package org.ktor_lecture.concertservice.application.port.out

import org.ktor_lecture.concertservice.domain.entity.ConcertEntity
import org.ktor_lecture.concertservice.domain.entity.ConcertUserEntity

interface ConcertWriteRepository {
    fun createUser(user: ConcertUserEntity)
    fun saveConcert(concert: ConcertEntity): ConcertEntity
}