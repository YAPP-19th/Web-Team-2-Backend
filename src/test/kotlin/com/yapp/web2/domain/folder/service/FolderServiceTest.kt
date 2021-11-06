package com.yapp.web2.domain.folder.service

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.entity.Urls
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.custom.FolderNotFoundException
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

    private lateinit var folder: Folder
    private lateinit var expected: String
    private lateinit var changeRequest: Folder.FolderNameChangeRequest

    @BeforeEach
    fun setup() {
        folder = Folder("Folder", 0, parentFolder = null)
        expected = "Update Folder"
        changeRequest = Folder.FolderNameChangeRequest(expected)
    }

    @Test
    fun `부모 폴더가 존재하지 않는 최상위 폴더 생성`() {
        // given
        val request = Folder.FolderCreateRequest(name = "Root Folder", index = 1)
        val expected = Folder.dtoToEntity(request)
        every { folderRepository.save(expected) } returns expected

        // when
        val actual = folderService.createFolder(request)

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

        every { folderRepository.findById(request.parentId).get() } returns parentFolder
        every { folderRepository.save(childFolder) } returns childFolder

        // when
        val actual = folderService.createFolder(request)
        val actual2 = actual.parentFolder

        // then
        assertAll(
            { assertThat(actual).isEqualTo(childFolder) },
            { assertThat(actual2).isEqualTo(parentFolder) }
        )
    }

    @Test
    fun `폴더 이름 수정`() {
        // given
        every { folderRepository.findByIdOrNull(any()) } returns folder
        every { folderRepository.save(any()) } returns folder

        // when
        val actual = folderService.changeFolderName(1L, changeRequest)

        // then
        assertAll(
            { assertDoesNotThrow { folderService.changeFolderName(10L, changeRequest) } },
            { assertThat(actual).isEqualTo(expected) }
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

        every { folderRepository.findById(1L).orElse(null) } returns prevParentFolder
        every { folderRepository.findById(2L).orElse(null) } returns nextParentFolder
        every { folderRepository.findById(10L).orElse(null) } returns prevMoveFolder
        every { folderRepository.findByIndexGreaterThan(prevParentFolder, 2) } returns stubPrevChildFolders
        every { folderRepository.findByIndexGreaterThan(nextParentFolder, 3) } returns stubNextChildFolders

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
        val bookmarks = makeBookmarks()
        every { bookmarkRepository.findByFolderId(1L) } returns bookmarks
        every { bookmarkRepository.delete(any()) } returns Unit

        // then
        assertAll(
            { assertDoesNotThrow { folderService.deleteAllBookmark(1L) } },
            { verify(exactly = 1) { bookmarkRepository.findByFolderId(any()) } },
            { verify(exactly = bookmarks.size) { bookmarkRepository.delete(any()) } }
        )
    }

    @Test
    fun `특정 폴더를 삭제한다`() {
        // given
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
        assertThrows<FolderNotFoundException> { folderService.changeFolderName(1L, changeRequest) }
    }

    private fun getParentFolder(name: String): Folder {
        return Folder(name, 0, 0, null)
    }

    private fun getChildFolders(parentFolder: Folder, start: Int, end: Int): MutableList<Folder> {
        val childFolders: MutableList<Folder> = mutableListOf()

        (start..end).forEach {
            val folder = Folder("$it 번 폴더", it, 0, parentFolder)
            childFolders.add(folder)
        }

        parentFolder.childrens = childFolders
        return childFolders
    }

    private fun makeBookmarks(): MutableList<Bookmark> {
        val bookmarks: MutableList<Bookmark> = mutableListOf()

        (0..4).forEach {
            bookmarks.add(Bookmark(it.toLong(), it.toLong(), it.toLong(), Urls()))
        }

        return bookmarks
    }

}