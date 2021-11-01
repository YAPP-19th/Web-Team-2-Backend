package com.yapp.web2.domain.bookmark.repository

import com.yapp.web2.domain.bookmark.entity.Bookmark
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase

@DataMongoTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class BookmarkRepositoryTest {
    @Autowired
    lateinit var bookmarkRepository: BookmarkRepository

    @Test
    fun `bookmark가 mongoDB에 저장된다`() {
        // given
        val bookmark = Bookmark(1, 1, "www.naver.com")

        //when, then
        Assertions.assertThat(bookmarkRepository.save(bookmark)).isEqualTo(bookmark)
    }
}