package dev.concert.infrastructure.slack

import com.slack.api.Slack
import dev.concert.domain.service.data.message.MessageManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SlackApiClient : MessageManager {

    @Value("\${slack.token}")
    lateinit var token : String

    @Value("\${slack.channel.id}")
    lateinit var channelId : String

    private val log : Logger = LoggerFactory.getLogger(SlackApiClient::class.java)

    override fun sendMessage(message : String) {
        val client = Slack.getInstance().methods()

        client.chatPostMessage {
            it.token(token)
                .channel(channelId)
                .text(message)
        }

        log.info("슬랙 메세지 전송 완료")
    }
}