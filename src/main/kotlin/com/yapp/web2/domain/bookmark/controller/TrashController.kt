package com.yapp.web2.domain.bookmark.controller

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.service.BookmarkPageService
import com.yapp.web2.domain.bookmark.service.BookmarkService
import com.yapp.web2.util.Message
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/trash")
class TrashController(
    private val bookmarkService: BookmarkService,
    private val bookmarkPageService: BookmarkPageService
) {
    @ApiOperation("북마크 복원 API")
    @PatchMapping("/restore")
    fun restoreBookmarks(@RequestBody @ApiParam(value = "복원할 북마크 ID 리스트", required = true) request: Bookmark.RestoreBookmarkRequest): ResponseEntity<String> {
        bookmarkService.restore(request.bookmarkIdList)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }

    @ApiOperation("북마크 영구삭제 API")
    @PostMapping("/truncate")
    fun permanentDelete(@RequestBody @ApiParam(value = "영구삭제할 북마크 ID 리스트", required = true) request: Bookmark.TruncateBookmarkRequest): ResponseEntity<String> {
        bookmarkService.permanentDelete(request.bookmarkIdList)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SUCCESS)
    }
}