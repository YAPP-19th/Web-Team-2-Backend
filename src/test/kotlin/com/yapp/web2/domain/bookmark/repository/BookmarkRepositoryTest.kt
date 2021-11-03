package com.yapp.web2.domain.bookmark.repository

import com.yapp.web2.domain.bookmark.entity.Bookmark
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.junit.jupiter.api.Assertions.*


@DataMongoTest
internal open class BookmarkRepositoryTest {
    @Autowired
    lateinit var bookmarkRepository: BookmarkRepository

    @Test
    open fun `bookmark가 mongoDB에 저장된다`() {
        // given
        val bookmark = Bookmark(1, 1, "www.naver.com")

        //when, then
        Assertions.assertThat(bookmarkRepository.save(bookmark)).isEqualTo(bookmark)
    }

    @Nested
    inner class PageNation {
        @Test
        fun `폴더 아이디에 해당하는 북마크들을 최신순으로 가져온다`() {
            // when
            val bookmarkPages = bookmarkRepository.findAllByFolderId(1, PageRequest.of(1, 5, Sort.by("saveTime").descending()))
            // then
            for (page in bookmarkPages)
                assertEquals(1, page.folderId)
        }

        @Test
        fun `폴더 아이디에 해당하는 북마크들 중 리마인드 시간이 설정된 북마크를 최신순으로 가져온다`() {
            //when
            val bookmarkPages = bookmarkRepository.findAllByFolderIdAndRemindTimeIsNotNull(1, PageRequest.of(1, 5, Sort.by("saveTime").descending()))

            //then
            for (page in bookmarkPages) {
                assertEquals(1, page.folderId)
                assertNotNull(page.remindTime)
            }
        }
    }

    @Nested
    inner class Search {

        @Test
        fun `사용자 아이디에 해당하는 북마크 중 검색어가 들어간 것을 제목과 url에서 검색한다`() {
            //given
            val testUserId: Long = 1
            val testKeyword = "nav"
            val pageAble = PageRequest.of(2, 4, Sort.by("saveTime").descending())

            //when
            val actualPages = bookmarkRepository.findByUserIdAndTitleContainingIgnoreCaseOrLinkContainingIgnoreCase(testUserId,
                testKeyword,
                testKeyword,
                pageAble)

            //then
            for (page in actualPages) {
                assertTrue(page.link.contains(testKeyword))
                when (page.title.isNullOrEmpty()) {
                    false -> assertTrue(page.title!!.contains(testKeyword))
                }
            }
        }
    }
}