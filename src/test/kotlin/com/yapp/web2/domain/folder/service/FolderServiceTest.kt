package com.yapp.web2.domain.folder.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.UserRepository
import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.custom.FolderNotFoundException
import com.yapp.web2.security.jwt.JwtProvider
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull

@ExtendWith(MockKExtension::class)
internal open class FolderServiceTest {

    @InjectMockKs
    private lateinit var folderService: FolderService

    @MockK
    private lateinit var folderRepository: FolderRepository

    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var jwtProvider: JwtProvider

    private lateinit var folder: Folder
    private lateinit var changeName: String
    private lateinit var changeEmoji: String
    private lateinit var changeNameRequest: Folder.FolderNameChangeRequest
    private lateinit var changeEmojiRequest: Folder.FolderEmojiChangeRequest
    private lateinit var user: Account

    @BeforeEach
    fun setup() {
        folder = Folder("Folder", 0, parentFolder = null)
        changeName = "Update Folder"
        changeEmoji = "️🥕🥕"
        changeNameRequest = Folder.FolderNameChangeRequest(changeName)
        changeEmojiRequest = Folder.FolderEmojiChangeRequest(changeEmoji)
        user = Account("test@gmail.com")
    }

    @Test
    fun `부모 폴더가 존재하지 않는 최상위 폴더 생성`() {
        // given
        val request = Folder.FolderCreateRequest(name = "Root Folder", index = 1)
        val expected = Folder.dtoToEntity(request)

        // mock
        every { jwtProvider.getIdFromToken(any()) } returns 1L
        every { userRepository.findByIdOrNull(any()) } returns user
        every { folderRepository.save(expected) } returns expected

        // when
        val actual = folderService.createFolder(request, "test")

        // then
        assertAll(
            { assertThat(actual).isEqualTo(expected) },
            { assertThat(actual.parentFolder).isNull() }
        )
    }

    @Test
    fun `부모 폴더가 존재하는 하위 폴더 생성`() {
        // given
        val parentFolder = Folder("Parent Folder", 2, 0, null)
        val request = Folder.FolderCreateRequest(2L, "Children Folder", 2)
        val childFolder = Folder.dtoToEntity(request, parentFolder)

        // mock
        every { jwtProvider.getIdFromToken(any()) } returns 1L
        every { userRepository.findByIdOrNull(any()) } returns user
        every { folderRepository.findById(request.parentId).get() } returns parentFolder
        every { folderRepository.save(childFolder) } returns childFolder

        // when
        val actual = folderService.createFolder(request, "test")
        val actual2 = actual.parentFolder

        // then
        assertAll(
            { assertThat(actual).isEqualTo(childFolder) },
            { assertThat(actual2).isEqualTo(parentFolder) }
        )
    }

    @Test
    fun `폴더 이름을 수정한다`() {
        // given & mock
        every { folderRepository.findByIdOrNull(any()) } returns folder
        every { folderRepository.save(any()) } returns folder

        // when
        val actual = folderService.changeFolderName(1L, changeNameRequest)

        // then
        assertAll(
            { assertDoesNotThrow { folderService.changeFolderName(10L, changeNameRequest) } },
            { assertThat(actual).isEqualTo(changeName) }
        )
    }

    @Test
    fun `폴더 이모지를 수정한다`() {
        // given & mock
        every { folderRepository.findByIdOrNull(any()) } returns folder
        every { folderRepository.save(any()) } returns folder

        // when
        val actual = folderService.changeEmoji(1L, changeEmojiRequest)

        // then
        assertAll(
            { assertDoesNotThrow { folderService.changeEmoji(10L, changeEmojiRequest) } },
            { assertThat(actual).isEqualTo(changeEmoji) }
        )
    }

    @Test
    fun `prevIndex가 2인 폴더가 nextIndex가 3인 폴더로 이동한다`() {
        // given & when
        val moveRequest = Folder.FolderMoveRequest(1L, 2L, 2, 3)
        val prevParentFolder = getParentFolder("이동 전 부모폴더")
        val nextParentFolder = getParentFolder("이동 후 부모폴더")
        val prevChildFolders = getChildFolders(prevParentFolder, 0, 9)
        val prevMoveFolder = prevChildFolders[2]

        // prevIndex, nextIndex 보다 큰 인덱스를 가진 폴더 리스트
        val stubPrevChildFolders = getChildFolders(prevParentFolder, 3, 9)
        val stubNextChildFolders = getChildFolders(nextParentFolder, 4, 9)

        // mock
        every { folderRepository.findById(1L).orElse(null) } returns prevParentFolder
        every { folderRepository.findById(2L).orElse(null) } returns nextParentFolder
        every { folderRepository.findById(10L).orElse(null) } returns prevMoveFolder
        every { folderRepository.findByIndexGreaterThanPrevFolder(prevParentFolder, 2) } returns stubPrevChildFolders
        every { folderRepository.findByIndexGreaterThanNextFolder(nextParentFolder, 3) } returns stubNextChildFolders

        // then
        assertAll(
            { assertDoesNotThrow { folderService.moveFolder(10L, moveRequest) } },
            { assertThat(stubPrevChildFolders.size).isNotEqualTo(stubNextChildFolders.size) },
            { assertThat(stubPrevChildFolders.size + 1).isEqualTo(stubNextChildFolders.size) },
            { assertThat(prevMoveFolder).isEqualTo(stubNextChildFolders[moveRequest.nextIndex]) }
        )
    }

    @Test
    fun `폴더에 존재하는 모든 북마크를 제거한다`() {
        // given & when
        val bookmarks: MutableList<Bookmark> = makeBookmarks()

        // mock
        every { bookmarkRepository.findByFolderId(1L) } returns bookmarks

        // then
        assertAll(
            { assertDoesNotThrow { folderService.deleteAllBookmark(1L) } },
            { verify(exactly = 1) { bookmarkRepository.findByFolderId(any()) } },
            { bookmarks.forEach {
                    assertThat(it.deleted).isEqualTo(true)
                    assertThat(it.folderId).isNull()
                }
            }
        )
    }

    @Test
    fun `특정 폴더를 삭제한다`() {
        // given & mock
        every { folderRepository.deleteById(any()) } returns Unit

        // when
        folderService.deleteFolder(3L)

        // then
        verify { folderService.deleteFolder(any()) }
    }

    @Test
    fun `존재하지 않는 폴더는 예외가 발생한다`() {
        // given & when
        every { folderRepository.findByIdOrNull(any()) }.throws(FolderNotFoundException())

        // then
        assertThrows<FolderNotFoundException> { folderService.changeFolderName(1L, changeNameRequest) }
    }

    @Test
    fun `전체 폴더를 조회하고 출력한다`() {
        // given
        val rootFolder1 = getParentFolder("부모폴더 1")
        val rootFolder2 = getParentFolder("부모폴더 2")
        rootFolder1.id = 1L
        rootFolder2.id = 2L
        rootFolder1.children = getChildFolders(rootFolder1, 0, 5)
        rootFolder2.children = getChildFolders(rootFolder2, 0, 6)
        val allFolder: MutableList<Folder> = mutableListOf(rootFolder1, rootFolder2)

        // mock
        every { jwtProvider.getIdFromToken(any()) } returns 1L
        every { userRepository.findById(any()).get() } returns user
        every { folderRepository.findAllByParentFolderIsNull(user) } returns allFolder

        // when
        val actual = folderService.findAll("test")

        // then
        printAllFolderToJson(actual)
    }

    private fun printAllFolderToJson(actual: Map<String, Any>) {
        val mapper = ObjectMapper()
        val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(actual)
        println(json)
    }

    private fun getParentFolder(name: String): Folder {
        return Folder(name, 0, 0, null)
    }

    private fun getChildFolders(parentFolder: Folder, start: Int, end: Int): MutableList<Folder> {
        val childFolders: MutableList<Folder> = mutableListOf()

        (start..end).forEach {
            val folder = Folder("${parentFolder.name}-$it", it, 0, parentFolder)
            folder.id = start.toLong()
            childFolders.add(folder)
        }

        parentFolder.children = childFolders
        return childFolders
    }

    private fun makeBookmarks(): MutableList<Bookmark> {
        val bookmarks: MutableList<Bookmark> = mutableListOf()

        (0..4).forEach {
            bookmarks.add(Bookmark(it.toLong(), it.toLong(), "www.naver.com"))
        }

        return bookmarks
    }

}
