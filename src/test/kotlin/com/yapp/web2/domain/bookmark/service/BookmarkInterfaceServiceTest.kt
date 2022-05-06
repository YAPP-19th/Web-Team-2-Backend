package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.bookmark.entity.BookmarkDto
import com.yapp.web2.domain.bookmark.entity.PersonalBookmark
import com.yapp.web2.domain.bookmark.entity.SharedBookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkInterfaceRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.security.jwt.JwtProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class BookmarkInterfaceServiceTest {
    @MockK
    private lateinit var bookmarkInterfaceRepository: BookmarkInterfaceRepository

    @MockK
    private lateinit var jwtProvider: JwtProvider

    @MockK
    private lateinit var folderRepository: FolderRepository

    private lateinit var bookmarkInterfaceService: BookmarkInterfaceService

    @BeforeEach
    internal fun init() {
        MockKAnnotations.init(this)
        bookmarkInterfaceService = BookmarkInterfaceService(bookmarkInterfaceRepository, folderRepository, jwtProvider)
    }

    @Nested
    inner class SaveBookmark {
        val testAccount = Account("test@email.com")
        val testDto =
            BookmarkDto.AddBookmarkDto("testLink.test.com", "testTitle", false, "testImage", "testDescription")
        val testToken = "testToken"
        val testFolder = Folder("testName", 0, 0, null)
        var testFolderId: Long? = 0L
        val testFolderEmoji = "testEmoji"

        @Test
        fun `모든 도토리에 추가되는 개인북마크를 저장한다`() {
            // given
            testAccount.id = 0L
            every { jwtProvider.getAccountFromToken(testToken) } returns testAccount
            every { bookmarkInterfaceRepository.save(any()) } returns PersonalBookmark(
                testAccount,
                testDto.link,
                testDto.title,
                testDto.image,
                testDto.description,
                null,
                null,
                testDto.remind
            )
            testFolderId = null

            // when
            val actualBookmark = bookmarkInterfaceService.addBookmark(testToken, testFolderId, testDto)

            // then
            assertEquals(PersonalBookmark::class, actualBookmark::class)
        }

        @Test
        fun `공유 북마크를 저장한다`() {
            // given
            testAccount.id = 0L
            testFolder.share = true
            every { jwtProvider.getAccountFromToken(testToken) } returns testAccount
            every { folderRepository.findFolderById(any()) } returns testFolder
            every { bookmarkInterfaceRepository.save(any()) } returns SharedBookmark(
                testAccount,
                testDto.link,
                testDto.title,
                testDto.image,
                testDto.description,
                null,
                null,
                testDto.remind
            )

            // when
            val actualBookmark = bookmarkInterfaceService.addBookmark(testToken, testFolderId, testDto)

            // then
            assertEquals(SharedBookmark::class, actualBookmark::class)
        }

        @Test
        fun `개인 북마크를 저장한다`() {
            //given
            testAccount.id = 0L
            testFolder.share = false
            every { jwtProvider.getAccountFromToken(testToken) } returns testAccount
            every { folderRepository.findFolderById(any()) } returns testFolder
            every { bookmarkInterfaceRepository.save(any()) } returns PersonalBookmark(
                testAccount,
                testDto.link,
                testDto.title,
                testDto.image,
                testDto.description,
                null,
                null,
                testDto.remind
            )

            // when
            val actualBookmark = bookmarkInterfaceService.addBookmark(testToken, testFolderId, testDto)

            // then
            assertEquals(PersonalBookmark::class, actualBookmark::class)
        }
    }
}