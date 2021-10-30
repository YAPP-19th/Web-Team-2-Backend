package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.entity.Information
import com.yapp.web2.domain.bookmark.entity.InformationDto
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.BusinessException
import com.yapp.web2.exception.ObjectNotFoundException
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.awt.print.Book
import java.time.LocalDateTime
import java.util.*

internal class BookmarkServiceTest {
    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var folderRepository: FolderRepository
    private lateinit var bookmarkService: BookmarkService

    @BeforeEach
    internal fun init() {
        MockKAnnotations.init(this)
        bookmarkService = BookmarkService(bookmarkRepository, folderRepository)
    }

    @Nested
    inner class SaveBookmark {
        private lateinit var bookmark: Bookmark
        private lateinit var information: Information
        private lateinit var informationDto: InformationDto
        private lateinit var folder: Folder
        private var folderId: Long = 0


        @BeforeEach
        internal fun setUp() {
            folder = Folder("test", "asdf", 0, mockk(), null, null)
            folderId = 1
            folder.id = folderId
            information = Information("www.naver.com", "", null)
            bookmark = Bookmark(1, 1, information)
            informationDto = InformationDto("www.naver.com")
        }

        @Test
        fun `폴더가 존재하고, 북마크를 추가한다`() {
            // given
            every { bookmarkRepository.save(any()) } returns bookmark
            every { folderRepository.findById(folderId) } returns Optional.of(folder)
            every { bookmarkRepository.findAllByFolderId(folderId) } returns emptyList()

            // when
            val addBookmark = bookmarkService.addBookmark(folderId, informationDto)

            // then
            assertThat(addBookmark).isEqualTo(bookmark)
        }

        @Test
        fun `북마크를 추가할 때, 폴더가 존재하지 않으면 예외를 던진다`() {
            //given
            every { folderRepository.findById(folderId) } returns Optional.empty()
            val predictException = ObjectNotFoundException("해당 폴더가 존재하지 않습니다.")

            //when
            val actualException = Assertions.assertThrows(ObjectNotFoundException::class.java) {
                bookmarkService.addBookmark(folderId, informationDto)
            }

            //then
            assertEquals(predictException.message, actualException.message)
        }

        @Test
        fun `같은 폴더에 같은 북마크가 존재한다면, 예외를 던진다`() {
            //given
            val sameUrlDto = InformationDto("www.naver.com")
            val predictException = BusinessException("똑같은 게 있어요.")
            every { folderRepository.findById(folderId) } returns Optional.of(folder)
            every { bookmarkRepository.findAllByFolderId(folderId) } returns listOf(bookmark)

            //when
            val actualException = Assertions.assertThrows(BusinessException::class.java) {
                bookmarkService.addBookmark(1, sameUrlDto)
            }

            //then
            assertEquals(predictException.message, actualException.message)
        }


        @Nested
        inner class DeleteBookmark {
            private lateinit var information: Information
            private lateinit var bookmark: Bookmark
            private val bookmarkId: Long = 1

            @BeforeEach
            internal fun setUp() {
                information = Information("www.naver.com", "test1", null)
                bookmark = Bookmark(1, 1, information)
                bookmark.id = bookmarkId
            }

            @Test
            fun `북마크가 존재하지 않으면 예외를 던진다`() {
                //given
                val predictException = BusinessException("없어요")
                every { bookmarkRepository.findById(bookmarkId) } returns Optional.empty()

                //when
                val actualException = Assertions.assertThrows(BusinessException::class.java) {
                    bookmarkService.deleteBookmark(bookmarkId)
                }

                //then
                assertEquals(predictException.message, actualException.message)
            }

            @Test
            fun `폴더가 존재하고, 삭제하고자하는 북마크를 삭제한다`() {
                //given
                every { bookmarkRepository.findById(bookmarkId) } returns Optional.of(bookmark)
                every { folderRepository.findById(folderId) } returns Optional.of(folder)

                //when+then
                assertDoesNotThrow { bookmarkService.deleteBookmark(bookmarkId) }
            }
        }

        @Nested
        inner class UpdateBookmark {
            private var testBookmarkId: Long = 0
            private lateinit var bookmark: Bookmark
            private lateinit var updateBookmarkDto: Bookmark.UpdateBookmarkDto
            private lateinit var information: Information

            @BeforeEach
            internal fun setUp() {
                information = Information("www.naver.com", "test2", LocalDateTime.now())
                bookmark = Bookmark(1, 1, information)
            }

            @Test
            fun `존재하지 않는 북마크라면 예외를 던진다`() {
                //given
                updateBookmarkDto = Bookmark.UpdateBookmarkDto("test2", true)
                every { bookmarkRepository.findById(testBookmarkId) } returns Optional.empty()
                val predictException = BusinessException("없어요")

                //when
                val actualException = Assertions.assertThrows(BusinessException::class.java) {
                    bookmarkService.updateBookmark(testBookmarkId, updateBookmarkDto)
                }

                //then
                assertEquals(predictException.message, actualException.message)
            }

            @Test
            fun `북마크의 title을 변경한다`() {
                //given
                val predictBookmark = Bookmark(1, 1, information)
                val updateBookmarkDto = Bookmark.UpdateBookmarkDto("test2", null)
                every { bookmarkRepository.findById(testBookmarkId) } returns Optional.of(bookmark)

                //when
                val actualBookmark = bookmarkService.updateBookmark(testBookmarkId, updateBookmarkDto)

                //then
                assertEquals(predictBookmark.information.title, actualBookmark.information.title)
            }

            @Test
            fun `북마크의 remind를 변경한다`() {
                //given
                val predictBookmark = Bookmark(1, 1, information)
                val updateBookmarkDto = Bookmark.UpdateBookmarkDto(null, false)
                every { bookmarkRepository.findById(testBookmarkId) } returns Optional.of(bookmark)

                //when
                val actualBookmark = bookmarkService.updateBookmark(testBookmarkId, updateBookmarkDto)

                //then
                assertEquals(predictBookmark.information.remindTime, actualBookmark.information.remindTime)

            }
        }
    }
}


