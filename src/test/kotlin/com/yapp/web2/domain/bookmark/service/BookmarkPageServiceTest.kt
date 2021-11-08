package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.ObjectNotFoundException
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.*

internal class BookmarkPageServiceTest {
    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var folderRepository: FolderRepository

    private lateinit var bookmarkPageService: BookmarkPageService

    private lateinit var testFolder: Folder
    private lateinit var testPageRequest: PageRequest
    private var testFolderId: Long = 1

    @BeforeEach
    internal fun init() {
        MockKAnnotations.init(this)
        bookmarkPageService = BookmarkPageService(bookmarkRepository, folderRepository)
        testPageRequest = PageRequest.of(1, 3, Sort.by("saveTime").descending())
        testFolder = Folder("Folder", 0, parentFolder = null)
    }

    @Test
    fun `폴더가 존재하지 않으면, 예외를 던진다`() {
        // given
        val expectException = ObjectNotFoundException("해당 폴더가 존재하지 않습니다.")
        every { folderRepository.findById(testFolderId) } returns Optional.empty()

        // when
        val actualException = assertThrows(ObjectNotFoundException::class.java) { bookmarkPageService.getAllPageByFolderId(testFolderId, testPageRequest, true) }

        //then
        assertEquals(expectException.message, actualException.message)
    }
}