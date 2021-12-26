package com.yapp.web2.infra.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class FirebaseInit {
    private val FIREBASE_CONFIG_PATH = "firebase-service-key.json"
    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() {
        val options: FirebaseOptions = FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(ClassPathResource(FIREBASE_CONFIG_PATH).inputStream))
            .build()

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
            log.info("Firebase Initialize..")
        }
    }

    @Bean
    fun app(): FirebaseApp {
        val option = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(ClassPathResource(FIREBASE_CONFIG_PATH).inputStream)).build()

        return FirebaseApp.initializeApp(option, "app")
    }

    @Bean
    fun firebaseAppExecutor(): ListeningExecutorService {
        return MoreExecutors.newDirectExecutorService()
    }
}