package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest

@DataMongoTest
internal class BookmarkSearchServiceTest {
    @Autowired
    lateinit var bookmarkSearchService: BookmarkSearchService

    @MockK
    lateinit var bookmarkRepository: BookmarkRepository
//
//    @Test
//    fun `사용자의 아이디가 존재하지 않는다면 예외를 던진다`() {
//        //TODO: jwtprovider를 먼저 만들어서 진행하자.
//    }
}