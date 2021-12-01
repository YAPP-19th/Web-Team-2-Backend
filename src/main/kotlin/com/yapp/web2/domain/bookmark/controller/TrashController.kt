package com.yapp.web2.domain.bookmark.controller

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.service.BookmarkService
import com.yapp.web2.util.Message
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/trash")
class TrashController(
    private val bookmarkService: BookmarkService
) {
    @PatchMapping("/restore")
    fun restoreBookmarks(@RequestBody request: Bookmark.RestoreBookmarkRequest): ResponseEntity<String> {
        bookmarkService.restore(request.bookmarkIdList)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @DeleteMapping("/truncate")
    fun permanentDelete(@RequestBody request: Bookmark.TruncateBookmarkRequest): ResponseEntity<String> {
        bookmarkService.permanentDelete(request.bookmarkIdList)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

}