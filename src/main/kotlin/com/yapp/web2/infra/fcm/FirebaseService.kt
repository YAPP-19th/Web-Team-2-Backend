package com.yapp.web2.infra.fcm

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FirebaseService {

    private val log = LoggerFactory.getLogger(javaClass)

    @Throws(Exception::class)
    fun sendMessage(targetToken: String, title: String, body: String): String {
        // 로컬 테스트
//        val firebaseInit = FirebaseInit()
//        firebaseInit.init()

        val notification = makeNotification(title, body)
        val message = makeMessage(targetToken, notification)
        val firebaseApp = FirebaseApp.getInstance("app")
        return FirebaseMessaging.getInstance(firebaseApp).send(message)
    }

    fun makeMessage(targetToken: String, notification: Notification): Message {
        return Message.builder()
            .setToken(targetToken)
            .setNotification(notification)
            .build()
    }

    fun makeNotification(title: String, body: String): Notification {
        return Notification(title, body)
    }
}