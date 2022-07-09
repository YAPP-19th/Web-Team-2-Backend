package com.yapp.web2.domain.folder.service

import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.security.jwt.JwtProvider
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class FolderMoveServiceTest {

    @InjectMockKs
    private lateinit var folderService: FolderService

    @MockK
    private lateinit var folderRepository: FolderRepository

    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var accountRepository: AccountRepository

    @MockK
    private lateinit var jwtProvider: JwtProvider

    private lateinit var user: Account

    @BeforeEach
    fun setup() {
        user = Account("test@gmail.com")
    }

    @DisplayName("FolderMoveWithEqualParentOrTopFolder")
    @Test
    fun `0번 보관함을 2번 보관함으로 이동한다`() {
        // given
        val moveRequest = Folder.FolderMoveRequest("root", 2)
        val topFolderList: MutableList<Folder> = mutableListOf()

        for (i in 0..4) {
            val folder = Folder("부모폴더 - $i", i, 0, null)
            folder.id = i.toLong()
            topFolderList.add(folder)
        }
        val moveFolder = topFolderList[0]

        // mock
        every { jwtProvider.getIdFromToken(any()) } returns 1L
        every { accountRepository.findById(any()).get() } returns user
        every { folderRepository.findById(any()).get() } returns moveFolder
        every { folderRepository.findAllByParentFolderIsNull(any()) } returns topFolderList

        // when
        folderService.moveFolderByDragAndDrop(0, moveRequest, "test")

        // then
        assertAll(
            { assertThat(topFolderList[0].index).isEqualTo(moveRequest.nextIndex) },
            { assertThat(topFolderList[0].index).isEqualTo(moveFolder.index) }
        )
    }

    @DisplayName("FolderMoveFromTopFolderToFolder")
    @Test
    fun `보관함에서 폴더로 이동`() {
    }

    @Test
    fun `폴더에서 보관함으로 이동`() {
    }

    @Test
    fun `폴더에서 폴더로 이동`() {
    }

    @Test
    fun `동일한 부모 내 폴더에서 폴더로 이동`() {
    }
}