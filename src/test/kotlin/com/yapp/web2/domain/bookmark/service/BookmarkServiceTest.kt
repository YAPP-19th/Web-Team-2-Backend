package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.entity.Url
import com.yapp.web2.domain.bookmark.entity.UrlDto
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.BusinessException
import com.yapp.web2.exception.GlobalExceptionHandler
import com.yapp.web2.exception.ObjectNotFoundException
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.util.*

internal class BookmarkServiceTest {
    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var folderRepository: FolderRepository
    private lateinit var bookmarkService: BookmarkService
    lateinit var globalExceptionHandler: GlobalExceptionHandler

    @BeforeEach
    internal fun init() {
        MockKAnnotations.init(this)
        globalExceptionHandler = GlobalExceptionHandler()
        bookmarkService = BookmarkService(bookmarkRepository, folderRepository)
    }

    @Nested
    inner class SaveUrl {
        private lateinit var bookmark: Bookmark
        private lateinit var url: Url
        private lateinit var urlDto: UrlDto

        @BeforeEach
        internal fun setUp() {
            url = Url("www.naver.com", "", 0)
            bookmark = Bookmark(1, 1, url)
            urlDto = UrlDto("www.naver.com")
        }

        @Test
        fun `폴더가 존재하고, URL을 추가한다`() {
            // given
            every { bookmarkRepository.save(any()) } returns bookmark

            // when
            val addBookmark = bookmarkService.addBookmark(1, urlDto)

            // then
            assertThat(addBookmark).isEqualTo(bookmark)
        }

        @Test
        fun `url을 추가할 때, 폴더가 존재하지 않으면 예외를 던진다`() {
            //given
            every { folderRepository.findById(1) } returns Optional.empty()
            val predictException = ObjectNotFoundException("해당 폴더가 존재하지 않습니다.")

            //when
            val actualException = Assertions.assertThrows(ObjectNotFoundException::class.java) {
                bookmarkService.addBookmark(1, urlDto)
            }

            //then
            Assertions.assertEquals(predictException.message, actualException.message)
        }

        @Test
        fun `같은 url이 존재한다면, 예외가 발생한다`() {

        }
    }


    @Test
    fun `폴더가 존재하고, 존재하는 URL을 삭제한다`() {

    }
}