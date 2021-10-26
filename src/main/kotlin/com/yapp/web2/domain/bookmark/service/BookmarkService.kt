package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.entity.Url
import com.yapp.web2.domain.bookmark.entity.UrlDto
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookmarkService(
    private val bookmarkRepository: BookmarkRepository
) {
    @Transactional
    fun addBookmark(folderId: Long, urlDto: UrlDto): Bookmark {
        // TODO: 폴더의 존재 확인, 없으면 예외
        val url = Url(urlDto.url, urlDto.title, 0)
        val bookmark = Bookmark(1, 1, url)
        return bookmarkRepository.save(bookmark)
    }
}