package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.bookmark.entity.BookmarkDto
import com.yapp.web2.domain.bookmark.entity.PersonalBookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkInterfaceRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.security.jwt.JwtProvider
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class PersonalBookmarkServiceTest {

    @MockK
    private lateinit var bookmarkInterfaceRepository: BookmarkInterfaceRepository

    @MockK
    private lateinit var folderRepository: FolderRepository

    @MockK
    private lateinit var jwtProvider: JwtProvider

    @InjectMockKs
    private lateinit var personalBookmarkService: PersonalBookmarkService

    private val testToken = "testToken"
    private val testFolderId = 1L

    private lateinit var folder: Folder
    private lateinit var account: Account

    @BeforeEach
    fun setup() {
        folder = Folder("Folder", 0, parentFolder = null)
        folder.id = testFolderId
        account = Account("testEmail")
        account.id = 1L
        account.remindCycle = 7
    }


    @Test
    fun `folder id가 존재하는 경우 북마크를 저장한다`() {
        // given
        val testDto = BookmarkDto.AddBookmarkDto("testLink", "testTitle", true, "testImage", "testDes")

        every { jwtProvider.getAccountFromToken(testToken) } returns account
        every { bookmarkInterfaceRepository.findAllByFolderId(testFolderId) } returns mutableListOf()
        every { folderRepository.findFolderById(testFolderId) } returns folder
        every { bookmarkInterfaceRepository.save(any()) } returns BookmarkDto.addBookmarkDtoToPersonalBookmark(testDto, account)

        // when
        val actual = personalBookmarkService.addBookmark(testToken, testFolderId, testDto)

        // then
        assertEquals(actual::class, PersonalBookmark::class)
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
        every { bookmarkInterfaceRepository.findAllByFolderId(testFolderId) } returns mutableListOf()
        every { bookmarkInterfaceRepository.save(any()) } returns BookmarkDto.addBookmarkDtoToPersonalBookmark(testDto, account)

        // when
        val actual = personalBookmarkService.addBookmark(testToken, folderId = null, testDto)

        // then
        assertEquals(actual::class, PersonalBookmark::class)
        assertEquals("testLink", actual.link)
        assertEquals("testTitle", actual.title)
        assertEquals("testImage", actual.image)
        assertEquals("testDes", actual.description)
    }

    @Test
    fun `북마크의 폴더 id가 존재하지 않아도, 북마크가 삭제된다`() {
        // given
        val bookmarkIdList = BookmarkDto.BookmarkIdList(mutableListOf("1", "2"))
        val testBookmark1 = PersonalBookmark()
        val testBookmark2 = PersonalBookmark()
        testBookmark1.id = "1"
        testBookmark2.id = "2"

        every { bookmarkInterfaceRepository.findAllById(bookmarkIdList.dotoriIdList) } returns mutableListOf(testBookmark1, testBookmark2)
        every { bookmarkInterfaceRepository.saveAll(mutableListOf(testBookmark1, testBookmark2)) } returns mutableListOf(testBookmark1, testBookmark2)

        // when
        personalBookmarkService.deleteBookmark(bookmarkIdList)

        // then
        assertTrue(testBookmark1.deleted)
        assertTrue(testBookmark2.deleted)
    }

    @Test
    fun `북마크의 폴더 id가 존재하여도, 북마크가 삭제된다`() {
        // given
        val bookmarkIdList = BookmarkDto.BookmarkIdList(mutableListOf("1", "2"))
        val testBookmark1 = PersonalBookmark()
        val testBookmark2 = PersonalBookmark()
        testBookmark1.id = "1"
        testBookmark2.id = "2"
        testBookmark1.folderId = testFolderId
        testBookmark2.folderId = testFolderId

        every { bookmarkInterfaceRepository.findAllById(bookmarkIdList.dotoriIdList) } returns mutableListOf(testBookmark1, testBookmark2)
        every { bookmarkInterfaceRepository.saveAll(mutableListOf(testBookmark1, testBookmark2)) } returns mutableListOf(testBookmark1, testBookmark2)
        every { folderRepository.findFolderById(testFolderId) } returns folder

        // when
        personalBookmarkService.deleteBookmark(bookmarkIdList)

        // then
        assertEquals(-2, folder.bookmarkCount)
    }

    @Test
    fun `북마크의 제목과 설명을 변경한다`() {
        // given
        val testDto = BookmarkDto.UpdateBookmarkDto("changeTitle", "changeDes")
        val testBookmark = PersonalBookmark()
        testBookmark.id = "1"

        every { bookmarkInterfaceRepository.findBookmarkInterfaceById("1") } returns testBookmark
        every { bookmarkInterfaceRepository.save(any()) } returns testBookmark

        // when
        personalBookmarkService.updateBookmark("1", testDto)

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
        val testBookmark1 = PersonalBookmark()
        val testBookmark2 = PersonalBookmark()
        testBookmark1.id = "1"
        testBookmark2.id = "2"
        testBookmark1.folderId = testFolderId
        testBookmark2.folderId = testFolderId
        nextFolder.id = nextFolderId

        every { bookmarkInterfaceRepository.findAllById(testBookmarkIdList) } returns mutableListOf(testBookmark1, testBookmark2)
        every { folderRepository.findFolderById(nextFolderId) } returns nextFolder
        every { folderRepository.findFolderById(testFolderId) } returns folder
        every { bookmarkInterfaceRepository.saveAll(mutableListOf(testBookmark1, testBookmark2)) } returns mutableListOf()

        // when
        personalBookmarkService.moveBookmarkList(testDto)

        // then
        assertEquals(nextFolderId, testBookmark1.folderId)
        assertEquals(-2, folder.bookmarkCount)
        assertEquals(nextFolderId, testBookmark1.folderId)
        assertEquals(2, nextFolder.bookmarkCount)
    }

    @Test
    fun `북마크를 리마인드한다`() {
        // given
        val testBookmarkId = "1"
        val testBookmark = PersonalBookmark()

        every { jwtProvider.getAccountFromToken(testToken) } returns account
        every { bookmarkInterfaceRepository.findBookmarkInterfaceById(testBookmarkId) } returns testBookmark
        every { bookmarkInterfaceRepository.save(any()) } returns testBookmark

        // when
        personalBookmarkService.toggleOnRemindBookmark(testToken, "1")

        // then
        assertTrue(!testBookmark.remindTime.isNullOrBlank())
    }

    @Test
    fun `북마크 리마인드를 해제한다`() {
        // given
        val testBookmarkId = "1"
        val testBookmark = PersonalBookmark()

        every { jwtProvider.getAccountFromToken(testToken) } returns account
        every { bookmarkInterfaceRepository.findBookmarkInterfaceById(testBookmarkId) } returns testBookmark
        every { bookmarkInterfaceRepository.save(any()) } returns testBookmark

        // when
        personalBookmarkService.toggleOffRemindBookmark(testToken, "1")

        // then
        assertEquals(null, testBookmark.remindTime)
    }

    @Test
    fun `복제된 북마크의 리마인드를 해제한다`() {
        // given
        val testBookmarkId = "1"
        val testBookmark = PersonalBookmark()
        testBookmark.parentBookmarkId = "3"

        every { jwtProvider.getAccountFromToken(testToken) } returns account
        every { bookmarkInterfaceRepository.findBookmarkInterfaceById(testBookmarkId) } returns testBookmark
        every { bookmarkInterfaceRepository.save(any()) } returns testBookmark
        every { bookmarkInterfaceRepository.delete(testBookmark) } returns Unit

        // when + then
        assertDoesNotThrow { (personalBookmarkService.toggleOffRemindBookmark(testToken, "1")) }
    }
}
