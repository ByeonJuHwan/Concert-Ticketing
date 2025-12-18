package org.ktor_lecture.concertservice.application.port.out

import org.ktor_lecture.concertservice.domain.entity.ConcertEntity
import org.ktor_lecture.concertservice.domain.entity.ConcertOptionEntity
import org.ktor_lecture.concertservice.domain.entity.ConcertUserEntity

interface ConcertWriteRepository {
    fun createUser(user: ConcertUserEntity): ConcertUserEntity
    fun saveConcert(concert: ConcertEntity): ConcertEntity
    fun deleteAll()
    fun saveAll(concertUsers: List<ConcertUserEntity>)
    fun saveConcertOption(concertOption: ConcertOptionEntity): ConcertOptionEntity
    fun deleteAllUser()
}