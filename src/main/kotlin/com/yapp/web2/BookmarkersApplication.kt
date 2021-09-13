package com.yapp.web2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BookmarkersApplication

fun main(args: Array<String>) {
    runApplication<BookmarkersApplication>(*args)
}
