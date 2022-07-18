package com.yapp.web2.domain.bookmark.service

import com.google.common.collect.ImmutableList
import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.domain.bookmark.BookmarkDto
import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.entity.Remind
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.custom.AlreadyExistRemindException
import com.yapp.web2.security.jwt.JwtProvider
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
internal open class BookmarkServiceTest {

    @InjectMockKs
    private lateinit var bookmarkService: BookmarkService

    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var jwtProvider: JwtProvider

    @MockK
    private lateinit var folderRepository: FolderRepository

    @MockK
    private lateinit var accountRepository: AccountRepository

    private lateinit var bookmark: Bookmark
    private lateinit var folder: Folder
    private lateinit var bookmarkDto: BookmarkDto.AddBookmarkDto
    private lateinit var account: Account

    private val testToken = "testToken"
    private val testFolderId = 1L

    @BeforeEach
    internal fun setUp() {
        folder = Folder("Folder", 0, parentFolder = null)
        folder.id = 0
        bookmarkDto = BookmarkDto.AddBookmarkDto("www.naver.com", "test", false, "test", "testDes")
        bookmark = Bookmark(0, 0, "www.naver.com")
        bookmark.id = "0"
        account = Account("testEmail")
        account.id = 0
        account.fcmToken = "testFcm"
    }

    @Test
    fun `foider id가 존재하는 경우 북마크를 저장한다`() {
        //given
        val testDto = BookmarkDto.AddBookmarkDto("testLink", "testTitle", true, "testImage", "testDes")

        every { jwtProvider.getAccountFromToken(testToken) } returns account
        every { bookmarkRepository.findAllByFolderId(testFolderId) } returns mutableListOf()
        every { folderRepository.findFolderById(testFolderId) } returns folder
        every { bookmarkRepository.save(any()) } returns BookmarkDto.addBookmarkDtoToBookmark(testDto, account)

        //when
        val actual = bookmarkService.addBookmark(testToken, testFolderId, testDto)

        //then
        assertEquals("testLink", actual.link)
        assertEquals("testTitle", actual.title)
        assertEquals("testImage", actual.image)
        assertEquals("testDes", actual.description)
        assertEquals(1, folder.bookmarkCount)
    }

    @Test
    fun `folder id가 존재하지 않는 경우 북마크를 저장한다`() {
        // given
        val testDto = BookmarkDto.AddBookmarkDto("testLink", "testTitle", true, "testImage", "testDes")

        every { jwtProvider.getAccountFromToken(testToken) } returns account
        every { bookmarkRepository.findAllByFolderId(testFolderId) } returns mutableListOf()
        every { bookmarkRepository.save(any()) } returns BookmarkDto.addBookmarkDtoToBookmark(testDto, account)

        // when
        val actual = bookmarkService.addBookmark(testToken, folderId = null, testDto)

        // then
        assertEquals("testLink", actual.link)
        assertEquals("testTitle", actual.title)
        assertEquals("testImage", actual.image)
        assertEquals("testDes", actual.description)
    }

    @Test
    fun `여러개의 북마크를 저장한다`() {
        // given
        val testDto1 = BookmarkDto.AddBookmarkDto("testLink1", "testTitle1", true, "testImage", "testDes")
        val testDto2 = BookmarkDto.AddBookmarkDto("testLink2", "testTitle2", true, "testImage", "testDes")
        val testDto3 = BookmarkDto.AddBookmarkDto("testLink3", "testTitle3", true, "testImage", "testDes")
        val bookmark1 = BookmarkDto.addBookmarkDtoToBookmark(testDto1, account)

        every { jwtProvider.getAccountFromToken(testToken) } returns account
        every { bookmarkRepository.save(any()) } returns bookmark1

        // when + then
        Assertions.assertDoesNotThrow {
            bookmarkService.addBookmarkList(
                testToken, folderId = null, BookmarkDto.AddBookmarkListDto(
                    ImmutableList.of(testDto1, testDto2, testDto3)
                )
            )
        }
    }

    @Test
    fun `북마크의 폴더 id가 존재하지 않아도, 북마크가 삭제된다`() {
        // given
        val bookmarkIdList = BookmarkDto.BookmarkIdList(mutableListOf("1", "2"))
        val testBookmark1 = Bookmark(0, null, "testLink1")
        val testBookmark2 = Bookmark(0, null, "testLink2")
        testBookmark1.id = "1"
        testBookmark2.id = "2"

        every { bookmarkRepository.findAllById(bookmarkIdList.dotoriIdList) } returns mutableListOf(
            testBookmark1,
            testBookmark2
        )
        every { bookmarkRepository.saveAll(mutableListOf(testBookmark1, testBookmark2)) } returns mutableListOf(
            testBookmark1,
            testBookmark2
        )

        // when
        bookmarkService.deleteBookmark(bookmarkIdList)

        // then
        Assertions.assertTrue(testBookmark1.deleted)
        Assertions.assertTrue(testBookmark2.deleted)
    }

    @Test
    fun `북마크의 폴더 id가 존재하여도, 북마크가 삭제된다`() {
        // given
        val bookmarkIdList = BookmarkDto.BookmarkIdList(mutableListOf("1", "2"))
        val testBookmark1 = Bookmark(0, null, "testLink1")
        val testBookmark2 = Bookmark(0, null, "testLink2")
        testBookmark1.id = "1"
        testBookmark2.id = "2"
        testBookmark1.folderId = testFolderId
        testBookmark2.folderId = testFolderId

        every { bookmarkRepository.findAllById(bookmarkIdList.dotoriIdList) } returns mutableListOf(
            testBookmark1,
            testBookmark2
        )
        every { bookmarkRepository.saveAll(mutableListOf(testBookmark1, testBookmark2)) } returns mutableListOf(
            testBookmark1,
            testBookmark2
        )
        every { folderRepository.findFolderById(testFolderId) } returns folder

        // when
        bookmarkService.deleteBookmark(bookmarkIdList)

        // then
        assertEquals(-2, folder.bookmarkCount)
    }

    @Test
    fun `북마크의 제목과 설명을 변경한다`() {
        // given
        val testDto = BookmarkDto.UpdateBookmarkDto("changeTitle", "changeDes")
        val testBookmark = Bookmark(0, null, "testLink1")
        testBookmark.id = "1"

        every { bookmarkRepository.findBookmarkById("1") } returns testBookmark
        every { bookmarkRepository.save(any()) } returns testBookmark

        // when
        bookmarkService.updateBookmark("1", testDto)

        // then
        assertEquals("changeTitle", testBookmark.title)
        assertEquals("changeDes", testBookmark.description)
    }

    @Test
    fun `북마크들을 이동시킨다`() {
        // given
        val nextFolder = Folder("testFolder", 0, 0, parentFolder = null)
        val nextFolderId = 2L
        val testBookmarkIdList = mutableListOf("1", "2")
        val testDto = BookmarkDto.MoveBookmarkDto(testFolderId, testBookmarkIdList, nextFolderId)
        val testBookmark1 = Bookmark(0, null, "testLink1")
        val testBookmark2 = Bookmark(0, null, "testLink2")
        testBookmark1.id = "1"
        testBookmark2.id = "2"
        testBookmark1.folderId = testFolderId
        testBookmark2.folderId = testFolderId
        nextFolder.id = nextFolderId

        every { bookmarkRepository.findAllById(testBookmarkIdList) } returns mutableListOf(testBookmark1, testBookmark2)
        every { folderRepository.findFolderById(nextFolderId) } returns nextFolder
        every { folderRepository.findFolderById(testFolderId) } returns folder
        every { bookmarkRepository.saveAll(mutableListOf(testBookmark1, testBookmark2)) } returns mutableListOf()

        // when
        bookmarkService.moveBookmarkList(testDto)

        // then
        assertEquals(nextFolderId, testBookmark1.folderId)
        assertEquals(-2, folder.bookmarkCount)
        assertEquals(nextFolderId, testBookmark1.folderId)
        assertEquals(2, nextFolder.bookmarkCount)
    }

    @Test
    fun `북마크를 리마인드한다`() {
        // given
        val testBookmarkId = "!"
        account.fcmToken = "testFcmToken"

        every { jwtProvider.getAccountFromToken(testToken) } returns account
        every { bookmarkRepository.findBookmarkById(testBookmarkId) } returns bookmark
        every { bookmarkRepository.save(bookmark) } returns bookmark

        // when
        bookmarkService.toggleOnRemindBookmark(testToken, testBookmarkId)

        // then
        assertEquals(1, bookmark.remindList.size)
    }

    @Test
    fun `북마크 리마인드를 해제한다`() {
        // given
        val testBookmarkId = "!"
        val testRemind = Remind(1L)
        bookmark.remindList.add(testRemind)
        account.id = 1L

        every { jwtProvider.getAccountFromToken(testToken) } returns account
        every { bookmarkRepository.findBookmarkById(testBookmarkId) } returns bookmark
        every { bookmarkRepository.save(bookmark) } returns bookmark

        // when
        bookmarkService.toggleOffRemindBookmark(testToken, testBookmarkId)

        // then
        assertEquals(0, bookmark.remindList.size)
    }

    @Test
    fun `북마크에 이미 remind를 한 경우 예외를 던진다`() {
        //given
        val testBookmarkId = "!"
        account.fcmToken = "testFcmToken"
        val testRemind = Remind(1L)
        bookmark.remindList.add(testRemind)
        account.id = 1L

        every { jwtProvider.getAccountFromToken(testToken) } returns account
        every { bookmarkRepository.findBookmarkById(testBookmarkId) } returns bookmark
        every { bookmarkRepository.save(bookmark) } returns bookmark

        //when + then
        assertThrows<AlreadyExistRemindException> { bookmarkService.toggleOnRemindBookmark(testToken, testBookmarkId) }
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