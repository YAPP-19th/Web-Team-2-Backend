package com.yapp.web2.infra.slack

interface SlackService {
    fun sendMessage(text: String)

    fun sendMessage(channel: String, text: String)
}