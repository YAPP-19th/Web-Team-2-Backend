package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.bookmark.entity.BookmarkDto
import com.yapp.web2.domain.bookmark.entity.SharedBookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkInterfaceRepository
import com.yapp.web2.domain.folder.entity.AccountFolder
import com.yapp.web2.domain.folder.entity.Authority
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.security.jwt.JwtProvider
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class SharedBookmarkServiceTest {
    @MockK
    private lateinit var bookmarkInterfaceRepository: BookmarkInterfaceRepository

    @MockK
    private lateinit var folderRepository: FolderRepository

    @MockK
    private lateinit var jwtProvider: JwtProvider

    @InjectMockKs
    private lateinit var sharedBookmarkService: SharedBookmarkService

    private lateinit var testAccount: Account
    private lateinit var rootFolder: Folder
    private lateinit var accountFolder: AccountFolder
    private lateinit var testBookmark: SharedBookmark

    private val folderId = 1L
    private val testToken = "testToken"

    @BeforeEach
    fun setup() {
        testAccount = Account("testEmail")
        rootFolder = Folder("testRootFolder", 0, parentFolder = null)
        rootFolder.id = folderId
        accountFolder = AccountFolder(testAccount, rootFolder)
        accountFolder.authority = Authority.INVITEE
        testAccount.accountFolderList = mutableListOf(accountFolder)
        testAccount.id = 1L
        testBookmark = SharedBookmark()
    }

    @Test
    fun `권한이 존재하는지 확인한다`() {
        // given
        every { folderRepository.findFolderById(folderId) } returns rootFolder

        // when + then
        assertDoesNotThrow { sharedBookmarkService.checkAuthority(testAccount, folderId) }
    }

    @Test
    fun `권한이 존재하지 않으면 예외를 던진다`() {
        // given
        accountFolder.authority = Authority.NONE
        testAccount.accountFolderList = mutableListOf(accountFolder)

        every { folderRepository.findFolderById(folderId) } returns rootFolder

        // when + then
        assertThrows<RuntimeException> { sharedBookmarkService.checkAuthority(testAccount, folderId) }
    }

    @Test
    fun `공유 보관함에 북마크를 추가한다`() {
        // given
        val testDto = BookmarkDto.AddBookmarkDto("testLink", "testTitle", true, "testImage", "testDes")

        every { folderRepository.findFolderById(folderId) } returns rootFolder
        every { bookmarkInterfaceRepository.findAllByFolderId(any()) } returns mutableListOf()
        every { jwtProvider.getAccountFromToken(testToken) } returns testAccount
        every { folderRepository.findFolderById(folderId) } returns rootFolder
        every { bookmarkInterfaceRepository.save(any()) } returns BookmarkDto.addBookmarkDtoToSharedBookmark(testDto, testAccount, rootFolder)

        // when
        val actual = sharedBookmarkService.addBookmark(testToken, folderId, testDto)

        // then
        assertAll(
            {Assertions.assertThat(actual.link).isEqualTo(testDto.link)},
            {Assertions.assertThat(actual.title).isEqualTo(testDto.title)},
            {Assertions.assertThat(actual.image).isEqualTo(testDto.image)},
            {Assertions.assertThat(actual.image).isEqualTo(testDto.image)},
            {Assertions.assertThat(rootFolder.bookmarkCount).isEqualTo(1)}
        )
    }

    @Test
    fun `공유 북마크를 삭제하면서 복제된 북마크들을 모두 삭제한다`() {
        // given

        // when

        // then
    }

    @Test
    fun `공유 북마크를 업데이트하면서 복제된 북마크들도 모두 업데이트한다`() {
        // given

        // when

        // then
    }

    @Test
    fun `공유 북마크를 같은 루트 폴더 안에서만 이동한다`() {
        // given

        // when

        // then
    }

    @Test
    fun `공유 북마크에서 리마인드 설정을 진행한다`() {
        // given

        // when

        // then
    }

    @Test
    fun `공유 북마크에서 리마인드 설정을 해제한다`() {
        // given

        // when

        // then
    }
}
