package org.ktor_lecture.concertservice.adapter.out.search.repository

import org.ktor_lecture.concertservice.adapter.out.search.document.ConcertDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ConcertSearchRepository: ElasticsearchRepository<ConcertDocument, String>, ConcertSearchRepositoryCustom {

    // 콘서트 이름 검색
    fun findByConcertNameContaining(concertName: String): List<ConcertDocument>

    // 가수 검색
    fun findBySingerContaining(singer: String): List<ConcertDocument>

    // 날짜 범위 검색
    fun findByStartDateBetween(from: LocalDate, to: LocalDate): List<ConcertDocument>
}