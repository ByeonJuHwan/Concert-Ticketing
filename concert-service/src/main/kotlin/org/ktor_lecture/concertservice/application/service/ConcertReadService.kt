package org.ktor_lecture.concertservice.application.service

import org.ktor_lecture.concertservice.application.port.`in`.GetConcertSuggestionUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SearchAvailableDatesUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SearchAvailableSeatUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SearchConcertUseCase
import org.ktor_lecture.concertservice.application.port.out.ConcertReadRepository
import org.ktor_lecture.concertservice.application.service.dto.ConcertDateInfo
import org.ktor_lecture.concertservice.application.service.dto.ConcertInfo
import org.ktor_lecture.concertservice.application.service.dto.ConcertSeatInfo
import org.ktor_lecture.concertservice.common.CacheManager
import org.ktor_lecture.concertservice.common.LocalCache
import org.ktor_lecture.concertservice.domain.annotation.ReadOnlyTransactional
import org.ktor_lecture.concertservice.domain.entity.ConcertOptionEntity
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ConcertReadService (
    private val concertReadRepository: ConcertReadRepository,
    private val cacheManager: CacheManager,
) : SearchConcertUseCase, SearchAvailableDatesUseCase, SearchAvailableSeatUseCase, GetConcertSuggestionUseCase {

    private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)

    /**
     * 콘서트를 조회한다
     *
     * 1. ElasticSearch 에서 조회
     * 2. 예외 발생시 DB Like 문 조횐
     */
    @ReadOnlyTransactional
    override fun getConcerts(concertName: String?, singer: String?, startDate: LocalDate?, endDate: LocalDate?): List<ConcertInfo> {
        val concerts = concertReadRepository.getConcerts(concertName, singer, startDate, endDate)
        return concerts.map { ConcertInfo.from(it) }
    }

    /**
     * 선택한 콘서트의 구체적인 정보를 조회한다
     * ex) 콘서트 장소, 콘서트 시작 날짜, 콘서트 종료 날짜...
     */
    @ReadOnlyTransactional
    override fun getAvailableDates(concertId: Long): List<ConcertDateInfo> {
        val cached = cacheManager.getOrPut(
            cache = LocalCache.MetaCache,
            key = "${ConcertDatesCache::class.java.simpleName}:${concertId}",
            clazz = ConcertDatesCache::class.java,
        ) {
            ConcertDatesCache.from(concertReadRepository.getAvailableDates(concertId))
        }

        if (cached == null) {
            log.info("Cache Miss : DB 직접 조회")
            return concertReadRepository.getAvailableDates(concertId)
                .map { ConcertDateInfo.from(it) }
        }

        log.info("캐시 Hit : 캐시 반환")

        return cached.dates.map { ConcertDateInfo.from(it) }
    }

    /**
     * 선택한 콘서트 날짜의 좌석 정보를 조회한다
     */
    @ReadOnlyTransactional
    override fun getAvailableSeats(concertOptionId: Long): List<ConcertSeatInfo> {
        val availableSeats = concertReadRepository.getAvailableSeats(concertOptionId)
        return availableSeats.map { ConcertSeatInfo.from(it) }
    }

    /**
     * 콘서트 검색어 자동완성 추천
     */
    override fun getConcertSuggestions(query: String): List<String> {
        return concertReadRepository.getConcertSuggestions(query)
    }
}

data class ConcertDatesCache(
    val dates: List<ConcertOptionEntity>
) {
    companion object {
        fun from(dates: List<ConcertOptionEntity>) = ConcertDatesCache(dates)
    }
}