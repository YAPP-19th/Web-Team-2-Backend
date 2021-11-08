package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BookmarkSearchService(
    private val bookmarkRepository: BookmarkRepository
) {
    fun searchKeywordOwnUserId(userId: Long, keyword: String, pageable: Pageable): Page<Bookmark> {
        //TODO: jwt를 통한 토큰 방식 먼저 구현하여 플로우에 맞게 처리하기
        //if (!isUserEnroll(userId))
        return bookmarkRepository.findByUserIdAndTitleContainingIgnoreCaseOrLinkContainingIgnoreCase(userId, keyword, keyword, pageable)
    }

    private fun isUserEnroll(userId: Long): Boolean {
        return true
    }
}