package dev.concert.infrastructure.slack

import com.slack.api.Slack
import dev.concert.domain.service.data.message.MessageManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SlackApiClient : MessageManager {

    private val log : Logger = LoggerFactory.getLogger(SlackApiClient::class.java)

    override fun sendMessage() {
        val client = Slack.getInstance().methods()
        runCatching {
            //TODO 슬랙 API 메시지 구성
        }.onFailure { e ->
            log.error("슬랙 메세지 전송 실패", e)
        }
    }
}