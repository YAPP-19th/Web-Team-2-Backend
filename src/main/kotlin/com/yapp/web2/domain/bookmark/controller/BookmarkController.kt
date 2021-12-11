package com.yapp.web2.domain.bookmark.controller

import com.yapp.web2.domain.bookmark.entity.Bookmark
import com.yapp.web2.domain.bookmark.service.BookmarkService
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
        return ResponseEntity.status(HttpStatus.OK).body("올라감")
    }

    @ApiOperation(value = "북마크 생성 API")
    @PostMapping("/{folderId}")
    fun createBookmark(
        request: HttpServletRequest,
        @PathVariable @ApiParam(value = "북마크를 지정할 폴더 ID", example = "12", required = true) folderId: Long,
        @RequestBody @ApiParam(value = "북마크 생성 정보", required = true) bookmark: Bookmark.AddBookmarkDto
    ): ResponseEntity<String> {
        val token = request.getHeader("AccessToken")
        bookmarkService.addBookmark(token, folderId, bookmark)
        return ResponseEntity.status(HttpStatus.OK).body("저장됨")
    }

    @ApiOperation(value = "북마크 삭제 API")
    @DeleteMapping("/{bookmarkId}")
    fun deleteBookmark(@PathVariable @ApiParam(value = "북마크 ID", example = "10", required = true) bookmarkId: String): ResponseEntity<String> {
        bookmarkService.deleteBookmark(bookmarkId)
        return ResponseEntity.status(HttpStatus.OK).body("삭제됨")
    }

    @ApiOperation(value = "북마크 수정 API")
    @PatchMapping("/{bookmarkId}")
    fun updateBookmark(
        @PathVariable @ApiParam(value = "북마크 ID", example = "10", required = true) bookmarkId: String,
        @RequestBody @Valid @ApiParam(value = "북마크 수정 정보", required = true) bookmark: Bookmark.UpdateBookmarkDto
    ): ResponseEntity<String> {
        bookmarkService.updateBookmark(bookmarkId, bookmark)
        return ResponseEntity.status(HttpStatus.OK).body("업데이트됨")
    }

    @ApiOperation(value = "북마크 이동 API")
    @PatchMapping("/move/{bookmarkId}")
    fun moveBookmark(
        @PathVariable @ApiParam(value = "북마크 ID", example = "10", required = true) bookmarkId: String,
        @RequestBody @ApiParam(value = "북마크 이동 정보", required = true) bookmark: Bookmark.MoveBookmarkDto
    ): ResponseEntity<String> {
        bookmarkService.moveBookmark(bookmarkId, bookmark)
        return ResponseEntity.status(HttpStatus.OK).body("폴더가 이동됨")
    }

}