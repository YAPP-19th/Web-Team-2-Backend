package com.yapp.web2.domain.bookmark.controller

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.service.BookmarkService
import com.yapp.web2.util.ControllerUtil
import com.yapp.web2.util.Message
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/bookmark")
class BookmarkController(
    private val bookmarkService: BookmarkService
) {
    @GetMapping("/click/{bookmarkId}")
    fun increaseBookmarkClickCount(@PathVariable bookmarkId: String): ResponseEntity<String> {
        bookmarkService.increaseBookmarkClickCount(bookmarkId)
        return ResponseEntity.status(HttpStatus.OK).body(Message.CLICK)
    }

    @ApiOperation(value = "북마크 생성 API")
    @PostMapping()
    fun createBookmark(
        request: HttpServletRequest,
        @RequestParam @ApiParam(value = "북마크를 지정할 폴더 ID", example = "12", required = true) folderId: Long?,
        @RequestBody @ApiParam(value = "북마크 생성 정보", required = true) bookmark: Bookmark.AddBookmarkDto
    ): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        bookmarkService.addBookmark(token, folderId, bookmark)
        return ResponseEntity.status(HttpStatus.OK).body(Message.SAVED)
    }

    @ApiOperation(value = "북마크 삭제 API")
    @PostMapping("/delete")
    fun deleteBookmark(@RequestBody bookmarkList: Bookmark.BookmarkIdList): ResponseEntity<String> {
        bookmarkService.deleteBookmark(bookmarkList)
        return ResponseEntity.status(HttpStatus.OK).body(Message.DELETED)
    }

    @ApiOperation(value = "북마크 수정 API")
    @PatchMapping("/{bookmarkId}")
    fun updateBookmark(
        request: HttpServletRequest,
        @PathVariable @ApiParam(value = "북마크 ID", example = "10", required = true) bookmarkId: String,
        @RequestBody @Valid @ApiParam(value = "북마크 수정 정보", required = true) bookmark: Bookmark.UpdateBookmarkDto
    ): ResponseEntity<String> {
        val token = ControllerUtil.extractAccessToken(request)
        bookmarkService.updateBookmark(token, bookmarkId, bookmark)
        return ResponseEntity.status(HttpStatus.OK).body(Message.UPDATED)
    }

    @PostMapping("/moveList")
    fun moveBookmarkList(
        @RequestBody moveBookmarkDto: Bookmark.MoveBookmarkDto
    ): ResponseEntity<String> {
        bookmarkService.moveBookmarkList(moveBookmarkDto)
        return ResponseEntity.status(HttpStatus.OK).body(Message.UPDATED)
    }

    @ApiOperation(value = "북마크 이동 API")
    @PatchMapping("/move/{bookmarkId}")
    fun moveBookmark(
        @PathVariable @ApiParam(value = "북마크 ID", example = "10", required = true) bookmarkId: String,
        @RequestBody @ApiParam(value = "북마크 이동 정보", required = true) bookmark: Bookmark.MoveBookmarkDto
    ): ResponseEntity<String> {
        bookmarkService.moveBookmark(bookmarkId, bookmark)
        return ResponseEntity.status(HttpStatus.OK).body(Message.MOVED)
    }

    @DeleteMapping("/remind/{bookmarkId}")
    fun releaseRemindBookmark(@PathVariable bookmarkId: String): ResponseEntity<String> {
        bookmarkService.releaseRemindBookmark(bookmarkId)
        return ResponseEntity.status(HttpStatus.OK).body(Message.UPDATED)
    }
}