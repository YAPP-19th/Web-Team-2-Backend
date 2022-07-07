package com.yapp.web2.domain.folder.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.yapp.web2.domain.account.entity.Account
import com.yapp.web2.domain.account.repository.AccountRepository
import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.AccountFolder
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.custom.AccountNotFoundException
import com.yapp.web2.exception.custom.FolderNotFoundException
import com.yapp.web2.security.jwt.JwtProvider
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.util.Optional

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
    fun `기본 폴더를 생성한다`() {
        // given
        val defaultFolder = Folder("보관함", index = 0, parentFolder = null)
        val account = Account("a@a.com")

        every { folderRepository.save(any()) } returns defaultFolder

        // when
        folderService.createDefaultFolder(account)

        // then
        verify(exactly = 1) { folderRepository.save(any()) }
    }

    @Test
    fun `폴더 생성 시 account가 존재하지 않으면 예외가 발생한다`() {
        // given
        val request = Folder.FolderCreateRequest(name = "Root Folder")
        every { jwtProvider.getIdFromToken(any()) } returns 1L
        every { accountRepository.findById(any()) } returns Optional.ofNullable(null)

        // then
        assertThrows<AccountNotFoundException> {  folderService.createFolder(request, "test") }
    }

    @Test
    fun `부모 폴더가 존재하지 않는 최상위 폴더를 생성한다`() {
        // given
        val request = Folder.FolderCreateRequest(name = "Root Folder")
        val expected = Folder.dtoToEntity(request, 0)

        every { jwtProvider.getIdFromToken(any()) } returns 1L
        every { accountRepository.findById(any()) } returns Optional.of(user)
        every { folderRepository.findAllByParentFolderCount(any()) } returns 0
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
    fun `부모 폴더가 존재하는 하위 폴더를 생성한다`() {
        // given
        val parentFolder = Folder("Parent Folder", 2, 0, null)
        parentFolder.id = 2L
        val request = Folder.FolderCreateRequest(2L, "Children Folder")
        val childFolder = Folder.dtoToEntity(request, parentFolder, 2)

        every { jwtProvider.getIdFromToken(any()) } returns 1L
        every { accountRepository.findById(any()) } returns Optional.of(user)
        every { folderRepository.findById(request.parentId).get() } returns parentFolder
        every { folderRepository.save(childFolder) } returns childFolder
        every { folderRepository.findAllByFolderCount(any(), any()) } returns 0

        // when
        val actual = folderService.createFolder(request, "test")

        // then
        assertAll(
            { assertThat(actual).isEqualTo(childFolder) },
            { assertThat(actual.parentFolder).isEqualTo(parentFolder) }
        )
    }

    @Test
    fun `폴더를 수정하면 하위의 모든 북마크들도 함께 수정된다`() {
        // given
        val bookmarkList: MutableList<Bookmark> = makeBookmarks()
        every { bookmarkRepository.findByFolderId(any()) } returns bookmarkList
        every { folderRepository.findByIdOrNull(any()) } returns folder
        every { bookmarkRepository.save(any()) } returns bookmarkList[0]

        // when
        folderService.changeFolder(1L, changeRequest)

        // then
        assertAll(
            { assertDoesNotThrow { folderService.changeFolder(10L, changeRequest) } },
            { assertThat(folder.name).isEqualTo(changeName) },
            { assertThat(folder.emoji).isEqualTo(changeEmoji) },
            {
                repeat(bookmarkList.size) {
                    assertThat(bookmarkList[it].folderName).isEqualTo(changeRequest.name)
                    assertThat(bookmarkList[it].folderEmoji).isEqualTo(changeRequest.emoji)
                }
            }
        )
    }

    @Test
    fun `0번째 보관함에서 3번째 보관함으로 폴더를 드래그 앤 드랍으로 이동한다`() {
        // given
        val moveFolder = Folder("Move Folder", 0, parentFolder = null)
        val request = Folder.FolderMoveRequest("root", 2)
        val topFolderList = getParentFolderList()

        every { jwtProvider.getIdFromToken(any()) } returns 1L
        every { accountRepository.findById(any()).get() } returns user
        every { folderRepository.findById(any()).get() } returns moveFolder
        every { folderRepository.findAllByParentFolderIsNull(any()) } returns topFolderList

        // when
        folderService.moveFolderByDragAndDrop(1L, request, "token")

        // then : topFolderList Index - 1,3,4 -> 0,3,4
        assertAll(
            { assertThat(moveFolder.index).isEqualTo(2) },
            { assertThat(topFolderList[0].index).isEqualTo(0)},
            { assertThat(topFolderList[1].index).isEqualTo(3) },
            { assertThat(topFolderList[2].index).isEqualTo(4) }
        )
    }

    @Test
    fun `3번째 보관함에서 0번째 보관함으로 폴더를 드래그 앤 드랍으로 이동한다`() {
        // given
        val moveFolder = Folder("Move Folder", 2, parentFolder = null)
        val request = Folder.FolderMoveRequest("root", 0)
        val topFolderList = getParentFolderList()

        every { jwtProvider.getIdFromToken(any()) } returns 1L
        every { accountRepository.findById(any()).get() } returns user
        every { folderRepository.findById(any()).get() } returns moveFolder
        every { folderRepository.findAllByParentFolderIsNull(any()) } returns topFolderList

        // when
        folderService.moveFolderByDragAndDrop(1L, request, "token")

        // then : topFolderList Index - 1,3,4 -> 2,3,4
        assertAll(
            { assertThat(moveFolder.index).isEqualTo(0) },
            { assertThat(topFolderList[0].index).isEqualTo(2)},
            { assertThat(topFolderList[1].index).isEqualTo(3) },
            { assertThat(topFolderList[2].index).isEqualTo(4) }
        )
    }

    @Test
    fun `이동하기 전 폴더가 존재하지 않을 경우 예외가 발생한다`() {
        // given
        val request = Folder.FolderMoveButtonRequest(mutableListOf(1L), 2L)
        every { jwtProvider.getIdFromToken(any()) } returns 1L
        every { accountRepository.findById(any()).get() } returns user
        every { folderRepository.findById(any()) } returns Optional.ofNullable(null)

        // then
        assertThrows<FolderNotFoundException> { folderService.moveFolderByButton("token", request) }
    }

    @Test
    fun `이동 하려는 폴더에 대한 정보가 존재하지 않을 경우 예외가 발생한다`() {
        // given
        val request = Folder.FolderMoveButtonRequest(mutableListOf(1L), 2L)
        every { jwtProvider.getIdFromToken(any()) } returns 1L
        every { accountRepository.findById(any()).get() } returns user
        every { folderRepository.findById(1L) } returns Optional.of(folder) // beforeFolder
        every { folderRepository.findAllByParentFolderIsNull(any()) } returns mutableListOf()
        every { folderRepository.findById(request.nextFolderId) } returns Optional.ofNullable(null) // nextFolder

        // then
        assertThrows<FolderNotFoundException> { folderService.moveFolderByButton("token", request) }
    }

    @Test
    fun `버튼을 클릭하여 0번째 폴더를 2번째 폴더로 이동한다`() {
        // given
        val beforeFolder = Folder("Before Folder", 0, parentFolder = null)
        val nextFolder = Folder("Next Folder", 2, parentFolder = null)
        beforeFolder.id = 1L
        nextFolder.id = 2L
        val beforeChildren = getParentFolderList()
        val request = Folder.FolderMoveButtonRequest(mutableListOf(1L), 2L)

        every { jwtProvider.getIdFromToken(any()) } returns 1L
        every { accountRepository.findById(any()).get() } returns user
        every { folderRepository.findById(1L) } returns Optional.of(beforeFolder)
        every { folderRepository.findById(2L) } returns Optional.of(nextFolder)
        every { folderRepository.findAllByParentFolderIsNull(any()) } returns beforeChildren
        every { folderRepository.save(any()) } returns folder

        // when
        folderService.moveFolderByButton("token", request)

        // then : 이동 전 폴더 리스트에서 자신보다 index가 큰 폴더들의 index가 1씩 감소한다
        assertAll(
            { assertThat(beforeChildren[0].index).isEqualTo(0)},
            { assertThat(beforeChildren[1].index).isEqualTo(2) },
            { assertThat(beforeChildren[2].index).isEqualTo(3) }
        )
    }

    private fun getParentFolderList(): MutableList<Folder> {
        return mutableListOf(
            Folder("Folder1", 1, parentFolder = null),
            Folder("Folder2", 3, parentFolder = null),
            Folder("Folder3", 4, parentFolder = null)
        )
    }

    @Test
    fun `folderId로 폴더를 조회한다`() {
        // given
        folder.id = 1L
        every { folderRepository.findById(1L) } returns Optional.of(folder)

        // when
        val actual = folderService.findByFolderId(1L)

        // then
        assertThat(actual).isEqualTo(folder)
    }

    @Test
    fun `폴더 조회 시 존재하지 않는 folderId일 경우 예외를 반환한다`() {
        // given
        every { folderRepository.findById(any()) } returns Optional.ofNullable(null)

        // then
        assertThrows<FolderNotFoundException> { folderService.findByFolderId(1L) }
    }

    @Test
    fun `폴더를 삭제한다`() {
        // given
        every { folderRepository.findByIdOrNull(any()) } returns folder
        every { folderRepository.deleteByFolder(folder) } just Runs

        // when
        folderService.deleteFolder(folder)

        // then
        assertAll(
            { verify(exactly = 1) { folderRepository.deleteByFolder(any()) } }
        )
    }

    @Test
    fun `존재하지 않는 폴더는 예외가 발생한다`() {
        // given
        every { folderRepository.findByIdOrNull(any()) }.throws(FolderNotFoundException())

        // then
        assertThrows<FolderNotFoundException> { folderService.changeFolder(1L, changeRequest) }
    }

    @Test
    fun `부모 폴더와 자식 폴더들을 전부 삭제한다`() {
        // given
        val parentFolder: Folder = getParentFolder("parent")
        parentFolder.id = 1L
        val childFolders: MutableList<Folder> = getChildFolders(parentFolder, 1, 5)
        val bookmark = makeBookmarks()

        every { bookmarkRepository.findByFolderId(any()) } returns bookmark
        every { folderRepository.deleteByFolder(any()) } just Runs
        every { bookmarkRepository.save(any()) } returns Bookmark(1L, 1L, "test")

        // when
        folderService.deleteFolderRecursive(parentFolder)

        // then
        verify(exactly = childFolders.size + 1) { folderRepository.deleteByFolder(any()) }
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
    fun `폴더 id를 정상적으로 암호화한다`() {
        // given
        val expected = "YanblGzXpM13KWrqVqhMYA=="
        folder.id = 1L
        every { folderRepository.findFolderById(any()) } returns folder
        every { jwtProvider.createFolderToken(any()) } returns expected

        // when
        val actual = folderService.encryptFolderId(folder.id!!)

        // then
        assertThat(actual.folderIdToken).isEqualTo(expected)
    }

    @Test
    fun `폴더 id를 암호화할 때, 폴더 id가 존재하지 않으면 예외를 던진다`() {
        // given
        folder.id = 1L
        every { folderRepository.findFolderById(folder.id!!) } returns null

        // then
        assertThrows<FolderNotFoundException> { folderService.encryptFolderId(folder.id!!) }
    }

    @Test
    fun `폴더를 삭제하면 북마크도 함께 삭제된다`() {
        // given
        val bookmarks1: MutableList<Bookmark> = makeBookmarks()
        val bookmarks2: MutableList<Bookmark> = makeBookmarks(5, 9)
        val deleteList = Folder.FolderListDeleteRequest(mutableListOf(1, 2))

        every { bookmarkRepository.findByFolderId(1) } returns bookmarks1
        every { bookmarkRepository.findByFolderId(2) } returns bookmarks2
        every { folderRepository.deleteById(any()) } just Runs
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
        rootParentFolder.id = 3L
        parentFolder.id = 2L
        folder.id = 1L
        folder.parentFolder = parentFolder
        parentFolder.parentFolder = rootParentFolder

        every { folderRepository.findByIdOrNull(any()) } returns folder

        // when
        val actual = folderService.findAllParentFolderList(1L)

        // then
        actual?.let { printJson(it) }
    }

    @Test
    fun `공유 폴더에 존재하는 모든 멤버에 대한 리스트를 준다`() {
        // given
        val folderId = 3L
        val testAccount = Account("test1")
        testAccount.id = 1
        testAccount.name = "test"
        testAccount.image = "testImage"

        folder.id = folderId
        val testList = mutableListOf(AccountFolder(testAccount, folder), AccountFolder(testAccount, folder) ,AccountFolder(testAccount, folder))
        folder.folders = testList

        every { folderRepository.findFolderById(folderId) } returns folder

        // when
        val actual = folderService.getAccountListAtRootFolder(folderId)

        // then
        assertThat(actual.list.size).isEqualTo(3)
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