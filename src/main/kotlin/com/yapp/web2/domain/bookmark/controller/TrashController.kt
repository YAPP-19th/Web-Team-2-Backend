package com.yapp.web2.domain.bookmark.controller

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.service.BookmarkPageService
import com.yapp.web2.domain.bookmark.service.BookmarkService
import com.yapp.web2.util.Message
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/v1/trash")
class TrashController(
    private val bookmarkService: BookmarkService,
    private val bookmarkPageService: BookmarkPageService
) {
    @PatchMapping("/restore")
    fun restoreBookmarks(@RequestBody request: Bookmark.RestoreBookmarkRequest): ResponseEntity<String> {
        bookmarkService.restore(request.bookmarkIdList)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @PostMapping("/truncate")
    fun permanentDelete(@RequestBody request: Bookmark.TruncateBookmarkRequest): ResponseEntity<String> {
        bookmarkService.permanentDelete(request.bookmarkIdList)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @GetMapping
    fun getTrashBookmark(
        request: HttpServletRequest,
        pageable: Pageable,
        @RequestParam remind: Boolean
    ): ResponseEntity<Page<Bookmark>> {
        val token = request.getHeader("AccessToken")
        return ResponseEntity.status(HttpStatus.OK).body(bookmarkPageService.getAllPageByUserId(token, pageable, remind))
    }
}