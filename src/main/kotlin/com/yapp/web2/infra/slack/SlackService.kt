package com.yapp.web2.infra.slack

interface SlackService {
    fun sendSlackAlarm(text: String)

    fun sendSlackAlarm(channel: String, text: String)
}