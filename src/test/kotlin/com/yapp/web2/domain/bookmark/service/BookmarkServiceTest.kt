package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.entity.Information
import com.yapp.web2.domain.bookmark.entity.InformationDto
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.domain.folder.entity.Folder
import com.yapp.web2.domain.folder.repository.FolderRepository
import com.yapp.web2.exception.BusinessException
import com.yapp.web2.exception.ObjectNotFoundException
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.util.*

internal class BookmarkServiceTest {
    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var folderRepository: FolderRepository
    private lateinit var bookmarkService: BookmarkService

    @BeforeEach
    internal fun init() {
        MockKAnnotations.init(this)
        bookmarkService = BookmarkService(bookmarkRepository, folderRepository)
    }

    @Nested
    inner class SaveBookmark {
        private lateinit var bookmark: Bookmark
        private lateinit var information: Information
        private lateinit var informationDto: InformationDto
        private lateinit var folder: Folder
        private var folderId: Long = 0


        @BeforeEach
        internal fun setUp() {
            folder = Folder("test", "asdf", mockk(), null, null)
            folderId = 1
            folder.id = folderId
            information = Information("www.naver.com", "", 0)
            bookmark = Bookmark(1, 1, information)
            informationDto = InformationDto("www.naver.com")
        }

        @Test
        fun `폴더가 존재하고, 북마크} 추가한다`() {
            // given
            every { bookmarkRepository.save(any()) } returns bookmark

            every { folderRepository.findById(folderId) } returns Optional.of(folder)
            every { bookmarkRepository.findAllByFolderId(folderId) } returns emptyList()

            // when
            val addBookmark = bookmarkService.addBookmark(folderId, informationDto)

            // then
            assertThat(addBookmark).isEqualTo(bookmark)
        }

        @Test
        fun `북마크를 추가할 때, 폴더가 존재하지 않으면 예외를 던진다`() {
            //given
            every { folderRepository.findById(folderId) } returns Optional.empty()
            val predictException = ObjectNotFoundException("해당 폴더가 존재하지 않습니다.")

            //when
            val actualException = Assertions.assertThrows(ObjectNotFoundException::class.java) {
                bookmarkService.addBookmark(folderId, informationDto)
            }

            //then
            Assertions.assertEquals(predictException.message, actualException.message)
        }

        @Test
        fun `같은 폴더에 같은 북마크가 존재한다면, 예외를 던진다`() {
            //given
            val sameUrlDto = InformationDto("www.naver.com")
            val predictException = BusinessException("똑같은 게 있어요.")
            every { folderRepository.findById(folderId) } returns Optional.of(folder)
            every { bookmarkRepository.findAllByFolderId(folderId) } returns listOf(bookmark)

            //when
            val actualException = Assertions.assertThrows(BusinessException::class.java) {
                bookmarkService.addBookmark(1, sameUrlDto)
            }

            //then
            Assertions.assertEquals(predictException.message, actualException.message)
        }


        @Nested
        inner class DeleteBookmark {
            private lateinit var information: Information
            private lateinit var bookmark: Bookmark
            private val bookmarkId: Long = 1

            @BeforeEach
            internal fun setUp() {
                information = Information("www.naver.com", "", 0)
                bookmark = Bookmark(1, 1, information)
                bookmark.id = bookmarkId
            }

            @Test
            fun `북마크가 존재하지 않으면 예외를 던진다`() {
                //given
                val predictException = BusinessException("없어요")
                every { bookmarkRepository.findById(bookmarkId) } returns Optional.empty()

                //when
                val actualException = Assertions.assertThrows(BusinessException::class.java) {
                    bookmarkService.deleteBookmark(bookmarkId)
                }

                //then
                Assertions.assertEquals(predictException.message, actualException.message)
            }

            @Test
            fun `폴더가 존재하고, 삭제하고자하는 북마크를 삭제한다`() {
                //given
                every { bookmarkRepository.findById(bookmarkId) } returns Optional.of(bookmark)
                every { bookmarkRepository.delete(bookmark) } returns Unit

                //when
                val deleteBookmark = bookmarkService.deleteBookmark(bookmarkId)

                //then
                Assertions.assertEquals(Unit, deleteBookmark)
            }
        }
    }
}


