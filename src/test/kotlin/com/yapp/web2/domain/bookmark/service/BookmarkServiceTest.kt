package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.bookmark.entity.*
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.*
import com.yapp.web2.exception.custom.BookmarkNotFoundException
import com.yapp.web2.exception.custom.SameBookmarkException
import com.yapp.web2.security.jwt.JwtProvider
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime
import java.util.*

internal class BookmarkServiceTest {
    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var jwtProvider: JwtProvider

    @MockK
    private lateinit var folderRepository: FolderRepository
    private lateinit var bookmarkService: BookmarkService

    @BeforeEach
    internal fun init() {
        MockKAnnotations.init(this)
        bookmarkService = BookmarkService(bookmarkRepository, folderRepository, jwtProvider)
    }

    @Nested
    inner class SaveBookmark {
        private lateinit var bookmark: Bookmark

        private lateinit var folder: Folder
        private lateinit var bookmarkDto: Bookmark.AddBookmarkDto
        private lateinit var token: String
        private lateinit var account: Account

        @BeforeEach
        internal fun setUp() {
            folder = Folder("Folder", 0, parentFolder = null)
            folder.id = 0
            token = "testToken"
            bookmarkDto = Bookmark.AddBookmarkDto("www.naver.com", "test", false, null, null)
            bookmark = Bookmark(0, 0, "www.naver.com")
            bookmark.id = "0"
            account = Account("testEmail")
            account.id = 0
            every { jwtProvider.getAccountFromToken(token) } returns account
        }

        @Test
        fun `북마크를 추가할 때, 폴더가 존재하지 않으면 예외를 던진다`() {
            //given
            every { folderRepository.findFolderById(folder.id!!) } returns null
            val predictException = ObjectNotFoundException()

            //when
            val actualException = Assertions.assertThrows(ObjectNotFoundException::class.java) {
                bookmarkService.addBookmark(token, folder.id!!, bookmarkDto)
            }

            //then
            assertEquals(predictException.message, actualException.message)
        }

        @Test
        fun `같은 폴더에 같은 북마크가 존재한다면, 예외를 던진다`() {
            //given
            val sameBookmarkDto = Bookmark.AddBookmarkDto("www.naver.com", "test", false, null, null)
            val predictException = SameBookmarkException()
            every { folderRepository.findFolderById(folder.id!!) } returns folder
            every { bookmarkRepository.findAllByFolderId(folder.id!!) } returns listOf(bookmark)

            //when
            val actualException = Assertions.assertThrows(BusinessException::class.java) {
                bookmarkService.addBookmark(token, 0, sameBookmarkDto)
            }

            //then
            assertEquals(predictException.message, actualException.message)
        }
    }

    @Nested
    inner class DeleteBookmark {
        private lateinit var bookmark: Bookmark
        private val bookmarkIdList = Bookmark.BookmarkIdList(mutableListOf(bookmark.id))
        private lateinit var folder: Folder
        private var folderId: Long = 0

        @BeforeEach
        internal fun setUp() {
            bookmark = Bookmark(1, 1, "www.naver.com")
            folder = Folder("Folder", 0, parentFolder = null)
            folderId = 1
        }

        @Test
        fun `북마크가 존재하지 않으면 예외를 던진다`() {
            //given
            val predictException = BookmarkNotFoundException()
            every { bookmarkRepository.findById(bookmark.id) } returns Optional.empty()

            //when
            val actualException = Assertions.assertThrows(BusinessException::class.java) {
                bookmarkService.deleteBookmark(bookmarkIdList)
            }

            //then
            assertEquals(predictException.message, actualException.message)
        }

        @Test
        fun `폴더가 존재하고, 삭제하고자하는 북마크를 삭제한다`() {
            //given
            every { bookmarkRepository.findById(bookmark.id) } returns Optional.of(bookmark)
            every { folderRepository.findById(folderId) } returns Optional.of(folder)
            every { bookmarkRepository.save(any()) } returns bookmark

            //when+then
            assertDoesNotThrow { bookmarkService.deleteBookmark(bookmarkIdList) }
        }
    }

    @Nested
    inner class UpdateBookmark {
        private var testBookmarkId: String = "0"
        private lateinit var bookmark: Bookmark

        private lateinit var updateBookmarkDto: Bookmark.UpdateBookmarkDto

        @BeforeEach
        internal fun setUp() {
            bookmark = Bookmark(1, 1, "www.naver.com")
            updateBookmarkDto = Bookmark.UpdateBookmarkDto("제목", false)
        }

        @Test
        fun `존재하지 않는 북마크라면 예외를 던진다`() {
            //given
            updateBookmarkDto = Bookmark.UpdateBookmarkDto("test2", true)
            every { bookmarkRepository.findById(testBookmarkId) } returns Optional.empty()
            val predictException = BusinessException("없어요")

            //when
            val actualException = Assertions.assertThrows(BusinessException::class.java) {
                bookmarkService.updateBookmark(testBookmarkId, bookmark.id, updateBookmarkDto)
            }

            //then
            assertEquals(predictException.message, actualException.message)
        }

        @Test
        fun `북마크의 title을 변경한다`() {
            //given
            val predictBookmark = Bookmark(1, 1, "www.naver.com", "test2")
            val updateBookmarkDto = Bookmark.UpdateBookmarkDto("test2", false)
            every { bookmarkRepository.findById(testBookmarkId) } returns Optional.of(bookmark)
            every { bookmarkRepository.save(any()) } returns bookmark

            //when
            val actualBookmark = bookmarkService.updateBookmark(testBookmarkId, bookmark.id, updateBookmarkDto)

            //then
            assertEquals(predictBookmark.title, actualBookmark.title)
        }

        @Test
        fun `북마크의 remind를 변경한다`() {
            //given
            val predictBookmark = Bookmark(1, 1, "www.naver.com")
            val updateBookmarkDto = Bookmark.UpdateBookmarkDto("test", false)
            every { bookmarkRepository.findById(testBookmarkId) } returns Optional.of(bookmark)
            every { bookmarkRepository.save(any()) } returns bookmark

            //when
            val actualBookmark = bookmarkService.updateBookmark(testBookmarkId, bookmark.id, updateBookmarkDto)

            //then
            assertEquals(predictBookmark.remindTime, actualBookmark.remindTime)
        }

        @Test
        fun `북마크의 clickCount를 올린다`() {
            //given
            every { bookmarkRepository.findById(testBookmarkId) } returns Optional.of(bookmark)
            every { bookmarkRepository.save(any()) } returns bookmark
            val predictClickCount = 1

            //when
            val actualBookmark = bookmarkService.increaseBookmarkClickCount(testBookmarkId)

            //then
            assertEquals(predictClickCount, actualBookmark.clickCount)
        }
    }

    @Nested
    inner class MoveBookmark {
        private lateinit var folder: Folder
        private var testBookmarkId: String = "0"
        private var prevFolderId: Long = 0
        private var nextFolderId: Long = 1
        private var moveBookmarkDto = Bookmark.MoveBookmarkDto(mutableListOf(testBookmarkId), nextFolderId)

        @BeforeEach
        fun init() {
            folder = Folder("Folder", 0, parentFolder = null)
        }

        @Test
        fun `북마크가 존재하지 않으면 예외를 던진다`() {
            //given
            val predictException = BusinessException("없어요")
            every { bookmarkRepository.findById(testBookmarkId) } returns Optional.empty()

            //when
            val actualException = Assertions.assertThrows(BusinessException::class.java) {
                bookmarkService.moveBookmark(testBookmarkId, moveBookmarkDto)
            }

            //then
            assertEquals(predictException.message, actualException.message)
        }

        @Test
        fun `같은 폴더로 변경한다면 변경하지 않는다`() {
            //given
            var sameFolderId: Long = 0
            var bookmark1 = Bookmark(1, 0, "www.naver.com")
            var testMoveBookmarkDto = Bookmark.MoveBookmarkDto(mutableListOf(testBookmarkId), sameFolderId)
            every { bookmarkRepository.findById(testBookmarkId) } returns Optional.of(bookmark1)
            every { folderRepository.findById(prevFolderId) } returns Optional.of(folder)
            every { folderRepository.findById(nextFolderId) } returns Optional.of(folder)
            every { bookmarkRepository.save(any()) } returns bookmark1

            //when
            bookmarkService.moveBookmark(testBookmarkId, testMoveBookmarkDto)

            //then
            assertEquals(sameFolderId, bookmark1.folderId)
        }

        @Test
        fun `다른 폴더로 url을 넘겨준다`() {
            //given
            var bookmark1 = Bookmark(1, 0, "www.naver.com")
            every { bookmarkRepository.findById(testBookmarkId) } returns Optional.of(bookmark1)
            every { folderRepository.findById(prevFolderId) } returns Optional.of(folder)
            every { folderRepository.findById(nextFolderId) } returns Optional.of(folder)
            every { bookmarkRepository.save(any()) } returns bookmark1

            //when
            bookmarkService.moveBookmark(testBookmarkId, moveBookmarkDto)

            //then
            assertEquals(bookmark1.folderId, nextFolderId)
        }
    }

    @Nested
    inner class RestoreBookmark {
        private val bookmark1 = Bookmark(1, 2, "www.naver.com")
        private val bookmark2 = Bookmark(2, 1, "www.naver.com")

        @Test
        fun `휴지통에서 북마크를 복원한다`() {
            // given
            val list = mutableListOf("1", "2")
            bookmark1.deleted = true
            bookmark1.deleteTime = LocalDateTime.now()
            bookmark2.deleted = true
            bookmark2.deleteTime = LocalDateTime.now()

            every { bookmarkRepository.findByIdOrNull("1") } returns bookmark1
            every { bookmarkRepository.findByIdOrNull("2") } returns bookmark2
            every { bookmarkRepository.save(any()) } returns bookmark1

            // when
            bookmarkService.restore(list)

            // then
            assertAll(
                { assertThat(bookmark1.deleted).isEqualTo(false) },
                { assertThat(bookmark1.deleteTime).isNull() },
                { assertThat(bookmark2.deleted).isEqualTo(false) },
                { assertThat(bookmark2.deleteTime).isNull() }
            )
        }
    }

    @Nested
    inner class TruncateBookmark {
        private val bookmark = Bookmark(1, 2, "www.naver.com")

        @Test
        fun `휴지통에서 북마크를 영구 삭제한다`() {
            // given
            val list = mutableListOf("1", "2")
            every { bookmarkRepository.findByIdOrNull(any()) } returns bookmark
            every { bookmarkRepository.delete(any()) } just Runs

            // when
            bookmarkService.permanentDelete(list)

            // then
            verify(exactly = list.size) { bookmarkRepository.delete(any()) }
        }
    }
}