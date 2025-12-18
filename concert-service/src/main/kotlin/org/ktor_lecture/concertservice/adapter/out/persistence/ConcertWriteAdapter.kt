package org.ktor_lecture.concertservice.adapter.out.persistence

import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.ConcertJpaRepository
import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.ConcertOptionJpaRepository
import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.ConcertUserJpaRepository
import org.ktor_lecture.concertservice.application.port.out.ConcertWriteRepository
import org.ktor_lecture.concertservice.domain.entity.ConcertEntity
import org.ktor_lecture.concertservice.domain.entity.ConcertOptionEntity
import org.ktor_lecture.concertservice.domain.entity.ConcertUserEntity
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ConcertWriteAdapter (
    private val concertUserJpaRepository: ConcertUserJpaRepository,
    private val concertJpaRepository: ConcertJpaRepository,
    private val concertOptionJpaRepository: ConcertOptionJpaRepository,
): ConcertWriteRepository {

    override fun createUser(user: ConcertUserEntity): ConcertUserEntity {
        return concertUserJpaRepository.save(user)
    }

    override fun saveConcert(concert: ConcertEntity): ConcertEntity {
        return concertJpaRepository.save(concert)
    }

    @Transactional
    override fun deleteAll() {
        concertJpaRepository.deleteAll()
    }

    @Transactional
    override fun saveAll(concertUsers: List<ConcertUserEntity>) {
        concertUserJpaRepository.saveAll(concertUsers)
    }

    @Transactional
    override fun saveConcertOption(concertOption: ConcertOptionEntity): ConcertOptionEntity {
        return concertOptionJpaRepository.save(concertOption)
    }

    override fun deleteAllUser() {
        concertUserJpaRepository.deleteAll()
    }
}