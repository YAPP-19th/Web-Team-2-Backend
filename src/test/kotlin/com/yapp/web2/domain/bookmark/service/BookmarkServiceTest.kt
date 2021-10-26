package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.entity.Url
import com.yapp.web2.domain.bookmark.entity.UrlDto
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class BookmarkServiceTest {
    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var bookmarkService: BookmarkService

    @BeforeEach
    internal fun init() {
        MockKAnnotations.init(this)
        bookmarkService = BookmarkService(bookmarkRepository)
    }

    @Nested
    inner class SaveUrl {
        private lateinit var bookmark: Bookmark
        private lateinit var url: Url

        @BeforeEach
        internal fun setUp() {
            url = Url("www.naver.com", "", 0)
            bookmark = Bookmark(1, 1, url)
        }

        @Test
        fun `폴더가 존재하고, URL을 추가한다`() {
            // given
            val urlDto = UrlDto("www.naver.com")
            every { bookmarkRepository.save(any()) } returns bookmark

            // when
            val addBookmark = bookmarkService.addBookmark(1, urlDto)

            // then
            Assertions.assertThat(addBookmark).isEqualTo(bookmark)
        }
    }


    @Test
    fun `폴더가 존재하고, 존재하는 URL을 삭제한다`() {

    }
}