package org.ktor_lecture.concertservice.adapter.`in`.consumer

import org.ktor_lecture.concertservice.adapter.out.kafka.KafkaTopics
import org.ktor_lecture.concertservice.application.port.out.ConcertReadRepository
import org.ktor_lecture.concertservice.application.service.cache.ConcertDatesCache
import org.ktor_lecture.concertservice.common.ActionType
import org.ktor_lecture.concertservice.common.CacheManager
import org.ktor_lecture.concertservice.common.JsonUtil
import org.ktor_lecture.concertservice.common.LocalCache
import org.ktor_lecture.concertservice.domain.event.ConcertOptionChangeEvent
import org.ktor_lecture.concertservice.domain.event.UserCreatedEvent
import org.ktor_lecture.concertservice.domain.exception.ConcertException
import org.ktor_lecture.concertservice.domain.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ConcertConsumer (
    private val cacheManager: CacheManager,
    private val concertReadRepository: ConcertReadRepository,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    val instanceId: String = UUID.randomUUID().toString()

    @KafkaListener(
        topics = [KafkaTopics.Concert.OPTION_CHANGED],
        groupId = "cache-invalidation-#{__listener.instanceId}",
        concurrency = "1",
    )
    fun concertOptionCacheRefreshConsumer(eventString: String ) {
        try {
            val event = JsonUtil.decodeFromJson<ConcertOptionChangeEvent>(eventString)

            val concertId = event.concertId
            val key = "${ConcertDatesCache::class.java.simpleName}:${concertId}"

            cacheManager.handleCacheByAction(ActionType.UPDATE, LocalCache.MetaCache, key) {
                ConcertDatesCache.from(concertReadRepository.getAvailableDates(concertId))
            }
        } catch (e: ConcertException) {
            log.error("ConcertException 발생", e)
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        } catch (e: Exception) {
            log.error("이벤트 처리 실패 : {}", eventString, e)
            throw e
        }
    }
}