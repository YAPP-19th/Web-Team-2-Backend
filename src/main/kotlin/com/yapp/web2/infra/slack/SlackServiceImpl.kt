package com.yapp.web2.infra.slack

import com.slack.api.Slack
import com.slack.api.methods.response.conversations.ConversationsListResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SlackServiceImpl : SlackService {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${slack.bot.token}")
    lateinit var token: String

    @Value("\${slack.monitor.channel.token}")
    lateinit var defaultChannel: String

    override fun sendMessage(text: String) {
        sendMessage(defaultChannel, text)
    }

    /**
     * Send Slack Alarm
     */
    override fun sendMessage(channelId: String, message: String) {
        val client = Slack.getInstance().methods()
        runCatching {
            client.chatPostMessage {
                it.token(token)
                    .channel(channelId)
                    .text(message)
            }
        }.onFailure { e ->
            log.error("Slack Send Error: {}", e.message, e)
        }
    }

    /**
     * Slack Channel name, id list
     */
    fun showSlackInfo() {
        val client = Slack.getInstance().methods()
        var result: ConversationsListResponse = ConversationsListResponse()
        kotlin.runCatching {
            result = client.conversationsList {
                it.token(token)
            }
        }.onSuccess {
            result.channels.stream().forEach {
                log.info("{} -> {}", it.name, it.id)
            }
        }
    }
}
