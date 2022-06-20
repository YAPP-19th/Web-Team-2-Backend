package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.AccountRepository
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
import org.junit.jupiter.api.Assertions.*
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

internal class BookmarkServiceTest {
    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var jwtProvider: JwtProvider

    @MockK
    private lateinit var folderRepository: FolderRepository

    @MockK
    private lateinit var accountRepository: AccountRepository
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
            val actualException = assertThrows(ObjectNotFoundException::class.java) {
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
            val actualException = assertThrows(BusinessException::class.java) {
                bookmarkService.addBookmark(token, folder.id!!, sameBookmarkDto)
            }

            //then
            assertEquals(predictException.message, actualException.message)
        }
    }

    @Nested
    inner class DeleteBookmark {
        private lateinit var bookmark: Bookmark
        private lateinit var bookmarkIdList: Bookmark.BookmarkIdList
        private lateinit var folder: Folder
        private var folderId: Long = 0

        @BeforeEach
        internal fun setUp() {
            bookmark = Bookmark(1, 1, "www.naver.com")
            folder = Folder("Folder", 0, parentFolder = null)
            bookmark.id = "0"
            folderId = 1
            bookmarkIdList = Bookmark.BookmarkIdList(mutableListOf(bookmark.id))
        }

        @Test
        fun `북마크가 존재하지 않으면 예외를 던진다`() {
            //given
            val predictException = BookmarkNotFoundException()
            every { bookmarkRepository.findBookmarkById(bookmark.id) } returns null

            //when
            val actualException = assertThrows(BookmarkNotFoundException::class.java) {
                bookmarkService.deleteBookmark(bookmarkIdList)
            }

            //then
            assertEquals(predictException.message, actualException.message)
        }
    }

    @Nested
    inner class UpdateBookmark {
        private lateinit var bookmark: Bookmark
        private lateinit var testToken: String
        private lateinit var updateBookmarkDto: Bookmark.UpdateBookmarkDto
        private lateinit var account: Account

        @BeforeEach
        internal fun setUp() {
            bookmark = Bookmark(1, 1, "www.naver.com")
            testToken = "testToken"
            updateBookmarkDto = Bookmark.UpdateBookmarkDto("제목", "test")
            account = Account("testEmail")
            account.id = 0
            bookmark.id = "0"
            every { jwtProvider.getAccountFromToken(testToken) } returns account
        }

        @Test
        fun `존재하지 않는 북마크라면 예외를 던진다`() {
            //given
            updateBookmarkDto = Bookmark.UpdateBookmarkDto("test2", "test")
            every { bookmarkRepository.findBookmarkById(bookmark.id) } returns null

            //when & then
            assertThrows(BookmarkNotFoundException::class.java) {
                bookmarkService.updateBookmark(bookmark.id, updateBookmarkDto)
            }
        }

        @Test
        fun `북마크의 title을 변경한다`() {
            //given
            val updateBookmarkDto = Bookmark.UpdateBookmarkDto("test2", "test2")
            val predictBookmark = bookmark
            predictBookmark.title = updateBookmarkDto.title
            predictBookmark.description = updateBookmarkDto.description
            every { bookmarkRepository.findBookmarkById(any()) } returns bookmark
            every { bookmarkRepository.save(predictBookmark) } returns predictBookmark

            //when
            val actualBookmark = bookmarkService.updateBookmark(bookmark.id, updateBookmarkDto)

            //then
            assertEquals(predictBookmark.title, actualBookmark.title)
        }

        @Test
        fun `북마크의 description을 변경한다`() {
            //given
            val updateBookmarkDto = Bookmark.UpdateBookmarkDto("test2", "test2")
            val predictBookmark = bookmark
            predictBookmark.title = updateBookmarkDto.title
            predictBookmark.description = updateBookmarkDto.description
            every { bookmarkRepository.findBookmarkById(any()) } returns bookmark
            every { bookmarkRepository.save(predictBookmark) } returns predictBookmark

            //when
            val actualBookmark = bookmarkService.updateBookmark(bookmark.id, updateBookmarkDto)

            //then
            assertEquals(predictBookmark.description, actualBookmark.description)
        }

        @Test
        fun `북마크 리마인드 설정할 때, remindList에 해당하는 fcmToken이 존재하면 예외를 던진다`() {
            //given
            val now = LocalDate.now()
            val testFcmToken = "testFcmToken"
            bookmark.remindList.add(Remind(now.toString(), testFcmToken))
            account.fcmToken = testFcmToken
            every { accountRepository.findAccountById(any()) } returns account
            every { bookmarkRepository.findBookmarkById(any()) } returns bookmark

            //when & then
            assertThrows(RuntimeException::class.java) {
                bookmarkService.toggleOnRemindBookmark(testToken, bookmark.id)
            }
        }

        @Test
        fun `북마크 리마인드 설정할 때, fcmToken이 존재하지 않으면 예외를 던진다`() {
            //given
            val now = LocalDate.now()
            val testFcmToken = "testFcmToken"
            bookmark.remindList.add(Remind(now.toString(), testFcmToken))
            every { accountRepository.findAccountById(any()) } returns account
            every { bookmarkRepository.findBookmarkById(any()) } returns bookmark

            //when & then
            assertThrows(RuntimeException::class.java) {
                bookmarkService.toggleOnRemindBookmark(testToken, bookmark.id)
            }
        }

        @Test
        fun `북마크 리마인드 설정할 때, remindList에 해당하는 remind가 추가된다`() {
            //given
            val testFcmToken = "testFcmToken"
            account.fcmToken = testFcmToken
            every { accountRepository.findAccountById(any()) } returns account
            every { bookmarkRepository.findBookmarkById(any()) } returns bookmark
            every { bookmarkRepository.save(any()) } returns bookmark

            //when
            bookmarkService.toggleOnRemindBookmark(testToken, bookmark.id)

            //then
            assertEquals(1, bookmark.remindList.size)
        }

        @Test
        fun `북마크 리마인드 해제할 때, remindList에 해당하는 fcmToken이 존재하지 않으면 예외를 던진다`() {
            //given
            val now = LocalDate.now()
            val testFcmToken = "testFcmToken"
            account.fcmToken = testFcmToken
            every { accountRepository.findAccountById(any()) } returns account
            every { bookmarkRepository.findBookmarkById(any()) } returns bookmark

            //when & then
            assertThrows(RuntimeException::class.java) {
            bookmarkService.toggleOffRemindBookmark(testToken, bookmark.id)
            }
        }

        @Test
        fun `북마크 리마인드 해제할 때, fcmToken이 존재하지 않으면 예외를 던진다`() {
            //given
            val now = LocalDate.now()
            val testFcmToken = "testFcmToken"
            bookmark.remindList.add(Remind(now.toString(), testFcmToken))
            every { accountRepository.findAccountById(any()) } returns account
            every { bookmarkRepository.findBookmarkById(any()) } returns bookmark

            //when & then
            assertThrows(RuntimeException::class.java) {
                bookmarkService.toggleOffRemindBookmark(testToken, bookmark.id)
            }
        }

        @Test
        fun `북마크 리마인드 해제할 때, remindList에 해당하는 remind가 삭제된다`() {
            //given
            val now = LocalDate.now()
            val testFcmToken = "testFcmToken"
            account.fcmToken = testFcmToken
            bookmark.remindList.add(Remind(now.toString(), testFcmToken))
            every { accountRepository.findAccountById(any()) } returns account
            every { bookmarkRepository.findBookmarkById(any()) } returns bookmark
            every { bookmarkRepository.save(any()) } returns bookmark

            //when
            bookmarkService.toggleOffRemindBookmark(testToken, bookmark.id)

            //then
            assertEquals(0, bookmark.remindList.size)
        }

        @Test
        fun `북마크의 clickCount를 올린다`() {
            //given
            every { bookmarkRepository.findBookmarkById(bookmark.id) } returns bookmark
            every { bookmarkRepository.save(any()) } returns bookmark
            val predictClickCount = 1

            //when
            val actualBookmark = bookmarkService.increaseBookmarkClickCount(bookmark.id)

            //then
            assertEquals(predictClickCount, actualBookmark.clickCount)
        }
    }

    @Nested
    inner class MoveBookmark {
        private lateinit var folder: Folder
        private lateinit var testBookmark: Bookmark
        private var testBookmarkId: String = "0"
        private var prevFolderId: Long = 0
        private var nextFolderId: Long = 1
        private var moveBookmarkDto = Bookmark.MoveBookmarkDto(mutableListOf(testBookmarkId), nextFolderId)

        @BeforeEach
        fun setUp() {
            folder = Folder("Folder", 0, parentFolder = null)
            testBookmark = Bookmark(1, 0, "www.naver.com")
            testBookmark.id = "0"
            every { bookmarkRepository.findBookmarkById(testBookmarkId) } returns testBookmark
            every { bookmarkRepository.save(any()) } returns testBookmark
            every { folderRepository.findFolderById(prevFolderId) } returns folder
            every { folderRepository.findFolderById(nextFolderId) } returns folder
        }

        @Test
        fun `같은 폴더로 변경한다면 변경하지 않는다`() {
            //given
            var sameFolderId: Long = 0
            var testMoveBookmarkDto = Bookmark.MoveBookmarkDto(mutableListOf(testBookmarkId), sameFolderId)

            //when
            bookmarkService.moveBookmark(testBookmarkId, testMoveBookmarkDto)

            //then
            assertEquals(sameFolderId, testBookmark.folderId)
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