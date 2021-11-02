package com.yapp.web2.domain.bookmark.controller

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.service.BookmarkPageService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class BookmarkController(
    private val bookmarkPageService: BookmarkPageService
) {
    @GetMapping("/api/v1/{folderId}")
    fun getBookmarkPage(@PathVariable folderId: Long, pageable: Pageable, @RequestParam remind: Boolean): Page<Bookmark> {
        return bookmarkPageService.getAllPageByFolderId(folderId, pageable, remind)
    }
}