package com.yapp.web2

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableBatchProcessing
class BookmarkersApplication

fun main(args: Array<String>) {
    runApplication<BookmarkersApplication>(*args)
}