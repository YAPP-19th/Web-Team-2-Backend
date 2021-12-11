package com.yapp.web2.domain.bookmark.service

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.repository.BookmarkRepository
import com.yapp.web2.security.jwt.JwtProvider
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BookmarkSearchService(
    private val bookmarkRepository: BookmarkRepository,
    private val jwtProvider: JwtProvider
) {
    fun searchKeywordOwnUserId(token: String, keyword: String, pageable: Pageable): Page<Bookmark> {
        //TODO: jwt를 통한 토큰 방식 먼저 구현하여 플로우에 맞게 처리하기
        val idFromToken = jwtProvider.getIdFromToken(token)
        //if (!isUserEnroll(userId))
        var bookmarkPage1 =
            bookmarkRepository.findByTitleContainingIgnoreCaseOrLinkContainingIgnoreCaseAndUserId(keyword, keyword, idFromToken, pageable)
        return bookmarkPage1
    }

//    private fun isUserEnroll(userId: Long): Boolean {
//        return true
//    }
}