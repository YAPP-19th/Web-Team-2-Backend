package com.yapp.web2.domain.folder.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.AccountRepository
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
    private lateinit var accountRepository: AccountRepository

    @MockK
    private lateinit var jwtProvider: JwtProvider

    private lateinit var folder: Folder
    private lateinit var changeEmoji: String
    private lateinit var changeName: String
    private lateinit var changeRequest: Folder.FolderChangeRequest
    private lateinit var user: Account

    @BeforeEach
    fun setup() {
        folder = Folder("Folder", 0, parentFolder = null)
        changeEmoji = "️🥕🥕"
        changeName = "Update Folder"
        changeRequest = Folder.FolderChangeRequest(changeEmoji, changeName)
        user = Account("test@gmail.com")
    }

    @Test
    fun `부모 폴더가 존재하지 않는 최상위 폴더 생성`() {
        // given
        val request = Folder.FolderCreateRequest(name = "Root Folder", index = 1)
        val expected = Folder.dtoToEntity(request)

        // mock
        every { jwtProvider.getIdFromToken(any()) } returns 1L
        every { accountRepository.findByIdOrNull(any()) } returns user
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
        every { accountRepository.findByIdOrNull(any()) } returns user
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

        // when
        folderService.changeFolder(1L, changeRequest)

        // then
        assertAll(
            { assertDoesNotThrow { folderService.changeFolder(10L, changeRequest) } },
            { assertThat(folder.name).isEqualTo(changeName) }
        )
    }

    @Test
    fun `폴더 이모지를 수정한다`() {
        // given & mock
        every { folderRepository.findByIdOrNull(any()) } returns folder

        // when
        folderService.changeFolder(1L, changeRequest)

        // then
        assertAll(
            { assertDoesNotThrow { folderService.changeFolder(10L, changeRequest) } },
            { assertThat(folder.emoji).isEqualTo(changeEmoji) }
        )
    }

    @Test
    fun `폴더 이름과 이모지를 수정한다`() {
        // given & mock
        every { folderRepository.findByIdOrNull(any()) } returns folder

        // when
        folderService.changeFolder(1L, changeRequest)

        // then
        assertAll(
            { assertDoesNotThrow { folderService.changeFolder(10L, changeRequest) } },
            { assertThat(folder.name).isEqualTo(changeName) },
            { assertThat(folder.emoji).isEqualTo(changeEmoji) }
        )
    }

    // TODO: 2021/12/18 폴더 수정 -> 폴더에 존재하는 북마크의 폴더 데이터 수정 테스트
    @Test
    fun `폴더에 존재하는 모든 북마크를 제거한다`() {
        // given & when
        val bookmarks: MutableList<Bookmark> = makeBookmarks()

        // mock
        every { bookmarkRepository.findByFolderId(1L) } returns bookmarks
        every { bookmarkRepository.save(any()) } returns bookmarks[0]

        // then
        assertAll(
            { verify(exactly = 1) { bookmarkRepository.findByFolderId(any()) } },
            { verify(exactly = bookmarks.size) { bookmarkRepository.save(any()) } },
            {
                bookmarks.forEach {
                    assertThat(it.deleted).isEqualTo(true)
                    assertThat(it.folderId).isNull()
                }
            }
        )
    }

    @Test
    fun `폴더를 삭제한다`() {
        // mock
        every { folderRepository.findByIdOrNull(any()) } returns folder
        every { folderRepository.deleteByFolder(folder) } returns Unit

        // when
        folderService.deleteFolder(folder)

        // then
        assertAll(
            { verify(exactly = 1) { folderRepository.deleteByFolder(any()) } }
        )
    }

    @Test
    fun `존재하지 않는 폴더는 예외가 발생한다`() {
        // given & when
        every { folderRepository.findByIdOrNull(any()) }.throws(FolderNotFoundException())

        // then
        assertThrows<FolderNotFoundException> { folderService.changeFolder(1L, changeRequest) }
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
        every { accountRepository.findById(any()).get() } returns user
        every { folderRepository.findAllByParentFolderIsNull(user) } returns allFolder
        every { folderRepository.findAllByAccount(user) } returns allFolder

        // when
        val actual = folderService.findAll("test")

        // then
        printJson(actual)
    }

    @Test
    fun `폴더를 삭제하면 북마크도 함께 삭제된다`() {
        // given
        val bookmarks1: MutableList<Bookmark> = makeBookmarks()
        val bookmarks2: MutableList<Bookmark> = makeBookmarks(5, 9)
        val deleteList = Folder.FolderListDeleteRequest(mutableListOf(1, 2))

        // mock
        every { bookmarkRepository.findByFolderId(1) } returns bookmarks1
        every { bookmarkRepository.findByFolderId(2) } returns bookmarks2
        every { folderRepository.deleteById(any()) } returns Unit
        every { bookmarkRepository.save(any()) } returns bookmarks1[0]

        // when
        folderService.deleteFolderList(deleteList)

        // then
        assertAll(
            { verify(exactly = deleteList.deleteFolderIdList.size) { folderRepository.deleteById(any()) } },
            {
                bookmarks1.forEach {
                    assertThat(it.folderId).isNull()
                    assertThat(it.deleted).isTrue()
                }
            },
            {
                bookmarks2.forEach {
                    assertThat(it.folderId).isNull()
                    assertThat(it.deleted).isTrue()
                }
            },
        )
    }

    @Test
    fun `특정 폴더에 대한 모든 자식들의 폴더 ID와 폴더명을 출력한다`() {
        // given
        val parentFolder = getParentFolder("부모 폴더")
        parentFolder.children = getChildFolders(parentFolder, 0, 4)

        // mock
        every { folderRepository.findByIdOrNull(any()) } returns parentFolder

        // when
        val actual = folderService.findFolderChildList(1L)

        // then
        printJson(actual)
    }

    @Test
    fun `특정 폴더에 대한 모든 부모들의 폴더 ID와 폴더명을 출력한다`() {
        // given
        val rootParentFolder = getParentFolder("최상위 부모 폴더")
        val parentFolder = getParentFolder("부모 폴더")
        val folder = getParentFolder("자식 폴더")
        rootParentFolder.id = 1L
        parentFolder.id = 2L
        folder.parentFolder = parentFolder
        parentFolder.parentFolder = rootParentFolder

        // mock
        every { folderRepository.findByIdOrNull(any()) } returns folder

        // when
        val actual = folderService.findAllParentFolderList(1L)

        // then
        actual?.let { printJson(it) }
    }

    private fun printJson(actual: Any) {
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
            folder.id = it.toLong()
            childFolders.add(folder)
        }

        parentFolder.children = childFolders
        return childFolders
    }

    private fun makeBookmarks(): MutableList<Bookmark> {
        return makeBookmarks(0, 4)
    }

    private fun makeBookmarks(start: Int, end: Int): MutableList<Bookmark> {
        val bookmarks: MutableList<Bookmark> = mutableListOf()

        (start..end).forEach {
            bookmarks.add(Bookmark(it.toLong(), it.toLong(), "www.naver.com"))
        }

        return bookmarks
    }

    private fun makeFolders(): MutableList<Folder> {
        val folders: MutableList<Folder> = mutableListOf()

        (0..1).forEach {
            folders.add(Folder("Folder - $it", it, 0, null))
        }

        return folders
    }
}